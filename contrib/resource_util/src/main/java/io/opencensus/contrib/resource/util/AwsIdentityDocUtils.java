/*
 * Copyright 2018, OpenCensus Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opencensus.contrib.resource.util;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/** Util methods for getting and parsing AWS instance identity document. */
final class AwsIdentityDocUtils {

  private static final int AWS_IDENTITY_DOC_BUF_SIZE = 0x800; // 2K chars (4K bytes)
  private static final String AWS_IDENTITY_DOC_LINE_BREAK_SPLITTER = "\n";
  private static final String AWS_IDENTITY_DOC_COLON_SPLITTER = ":";

  private static final URI AWS_INSTANCE_IDENTITY_DOCUMENT_URI =
      URI.create("http://169.254.169.254/latest/dynamic/instance-identity/document");

  private static final Map<String, String> awsEnvVarMap = initializeAwsIdentityDocument();

  static boolean isRunningOnAws() {
    return awsEnvVarMap != null;
  }

  // Tries to establish an HTTP connection to AWS instance identity document url. If the application
  // is running on an EC2 instance, we should be able to get back a valid JSON document. Parses that
  // document and stores the identity properties in a local map.
  // This method should only be called once.
  private static Map<String, String> initializeAwsIdentityDocument() {
    InputStream stream = null;
    try {
      stream = openStream(AWS_INSTANCE_IDENTITY_DOCUMENT_URI);
      String awsIdentityDocument = slurp(new InputStreamReader(stream, Charset.forName("UTF-8")));
      return parseAwsIdentityDocument(awsIdentityDocument);
    } catch (IOException e) {
      // Cannot connect to http://169.254.169.254/latest/dynamic/instance-identity/document.
      // Not on an AWS EC2 instance.
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          // Do nothing.
        }
      }
    }
    return null;
  }

  /** quick http client that allows no-dependency try at getting instance data. */
  private static InputStream openStream(URI uri) throws IOException {
    HttpURLConnection connection = HttpURLConnection.class.cast(uri.toURL().openConnection());
    connection.setConnectTimeout(1000 * 2);
    connection.setReadTimeout(1000 * 2);
    connection.setAllowUserInteraction(false);
    connection.setInstanceFollowRedirects(false);
    return connection.getInputStream();
  }

  /** returns the {@code reader} as a string without closing it. */
  private static String slurp(Reader reader) throws IOException {
    StringBuilder to = new StringBuilder();
    CharBuffer buf = CharBuffer.allocate(AWS_IDENTITY_DOC_BUF_SIZE);
    while (reader.read(buf) != -1) {
      buf.flip();
      to.append(buf);
      buf.clear();
    }
    return to.toString();
  }

  // AWS Instance Identity Document is a JSON file.
  // See docs.aws.amazon.com/AWSEC2/latest/UserGuide/instance-identity-documents.html.
  static Map<String, String> parseAwsIdentityDocument(String awsIdentityDocument) {
    Map<String, String> map = new HashMap<String, String>();
    @SuppressWarnings("StringSplitter")
    String[] lines = awsIdentityDocument.split(AWS_IDENTITY_DOC_LINE_BREAK_SPLITTER, -1);
    for (String line : lines) {
      @SuppressWarnings("StringSplitter")
      String[] keyValuePair = line.split(AWS_IDENTITY_DOC_COLON_SPLITTER, -1);
      if (keyValuePair.length != 2) {
        continue;
      }
      String key = keyValuePair[0].replaceAll("[\" ]", "");
      String value = keyValuePair[1].replaceAll("[\" ,]", "");
      map.put(key, value);
    }
    return map;
  }

  private static String getValueFromAwsIdentityDocument(String key) {
    if (awsEnvVarMap == null) {
      return "";
    }
    return firstNonNull(awsEnvVarMap.get(key), "");
  }

  static String getAccountId() {
    return getValueFromAwsIdentityDocument("accountId");
  }

  static String getRegion() {
    return getValueFromAwsIdentityDocument("region");
  }

  static String getAvailabilityZone() {
    return getValueFromAwsIdentityDocument("availabilityZone");
  }

  static String getInstanceId() {
    return getValueFromAwsIdentityDocument("instanceId");
  }

  static String getMachineType() {
    return getValueFromAwsIdentityDocument("instanceType");
  }

  private AwsIdentityDocUtils() {}
}
