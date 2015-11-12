/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtbuf.codec;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Implementation of a Marshaller for byte arrays
 * 
 */
public class BytesCodec implements Codec<byte[]> {

    public static final BytesCodec INSTANCE = new BytesCodec();

    public void encode(byte[] data, DataOutput dataOut) throws IOException {
        dataOut.writeInt(data.length);
        dataOut.write(data);
    }

    public byte[] decode(DataInput dataIn) throws IOException {
        int size = dataIn.readInt();
        byte[] data = new byte[size];
        dataIn.readFully(data);
        return data;
    }
    
    public int getFixedSize() {
        return -1;
    }

    public byte[] deepCopy(byte[] source) {
        byte []rc = new byte[source.length];
        System.arraycopy(source, 0, rc, 0, source.length);
        return rc;
    }

    public boolean isDeepCopySupported() {
        return true;
    }

    public boolean isEstimatedSizeSupported() {
        return true;
    }

    public int estimatedSize(byte[] object) {
        return object.length+4;
    }
}
