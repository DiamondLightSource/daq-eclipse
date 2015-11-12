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
package org.fusesource.hawtbuf;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class Buffer implements Comparable<Buffer> {

    public byte[] data;
    public int offset;
    public int length;

    public Buffer(ByteBuffer other) {
        this(other.array(), other.arrayOffset()+other.position(), other.remaining());
    }

    public Buffer(Buffer other) {
        this(other.data, other.offset, other.length);
    }

    public Buffer(int size) {
        this(new byte[size]);
    }
    
    public Buffer(byte data[]) {
        this(data, 0, data.length);
    }

    public Buffer(byte data[], int offset, int length) {
        assert data!=null : "data cannot be null";
        assert offset+length <= data.length : String.format("offset %d + length %d must be <= the data.length %d",offset, length, data.length);
        this.data = data;
        this.offset = offset;
        this.length = length;
    }

    public String hex() {
        return HexSupport.toHexFromBuffer(this);
    }

    public final Buffer flip() {
        length = offset;
        offset = 0;
        return this;
    }

    public final Buffer moveHead(int value) {
        assert value <= length : "Head position cannot be advanced past the tail";
        int newOffset = offset+value;
        assert newOffset >= 0 : "Head position cannot be moved back past the start of the buffer";
        offset = newOffset;
        length -= value;
        return this;
    }

    public final Buffer moveTail(int value) {
        int newLength = length+value;
        assert offset+newLength <= data.length : "Tail position cannot be advanced past the end of the buffer";
        assert newLength >= 0 : "Tail position cannot be moved back past head of the buffer";
        length = newLength;
        return this;
    }

    public final Buffer clear() {
        length = data.length;
        offset = 0;
        return this;
    }

    public final Buffer slice(int low, int high) {
        int sz;
        if (high < 0) {
            sz = length + high;
        } else {
            sz = high - low;
        }
        if (sz < 0) {
            sz = 0;
        }
        return new Buffer(data, (offset + low), sz);
    }

    public final byte[] getData() {
        return data;
    }
    public final Buffer data(byte[] data) {
        this.data = data;
        return this;
    }

    public final int getLength() {
        return length;
    }

    public final int length() {
        return length;
    }
    public final Buffer length(int length) {
        this.length = length;
        return this;
    }

    public final int getOffset() {
        return offset;
    }
    public final Buffer offset(int offset) {
        this.offset = offset;
        return this;
    }

    final public Buffer deepCopy() {
        byte t[] = new byte[length];
        System.arraycopy(data, offset, t, 0, length);
        return new Buffer(t);
    }

    final public Buffer compact() {
        if (length != data.length) {
            return new Buffer(toByteArray());
        }
        return this;
    }

    final public byte[] toByteArray() {
        byte[] data = this.data;
        int length = this.length;
        if (length != data.length) {
            byte t[] = new byte[length];
            System.arraycopy(data, offset, t, 0, length);
            data = t;
        }
        return data;
    }

    final public byte get(int i) {
        return data[offset + i];
    }

    final public boolean equals(Buffer obj) {
        byte[] data = this.data;
        int offset = this.offset;
        int length = this.length;

        if (length != obj.length) {
            return false;
        }
        
        byte[] objData = obj.data;
        int objOffset = obj.offset;
        
        for (int i = 0; i < length; i++) {
            if (objData[objOffset + i] != data[offset + i]) {
                return false;
            }
        }
        return true;
    }

    final public BufferInputStream in() {
        return new BufferInputStream(this);
    }

    final public BufferOutputStream out() {
        return new BufferOutputStream(this);
    }

    final public BufferEditor bigEndianEditor() {
        return new BufferEditor.BigEndianBufferEditor(this);
    }

    final public BufferEditor littleEndianEditor() {
        return new BufferEditor.LittleEndianBufferEditor(this);
    }

    final public boolean isEmpty() {
        return length == 0;
    }

    final public boolean contains(byte value) {
        return indexOf(value, 0) >= 0;
    }

    final public int indexOf(byte value) {
        return indexOf(value, 0);
    }
    
    final public int indexOf(byte value, int pos) {
        byte[] data = this.data;
        int offset = this.offset;
        int length = this.length;
        
        for (int i = pos; i < length; i++) {
            if (data[offset + i] == value) {
                return i;
            }
        }
        return -1;
    }

    final public boolean startsWith(Buffer other) {
        return indexOf(other, 0)==0;
    }
    
    final public int indexOf(Buffer needle) {
        return indexOf(needle, 0);
    }
    
    final public int indexOf(Buffer needle, int pos) {
        int max = length - needle.length;
        for (int i = pos; i <= max; i++) {
            if (matches(needle, i)) {
                return i;
            }
        }
        return -1;
    }
    
    final public boolean containsAt(Buffer needle, int pos) {
        if( (length-pos) < needle.length ) {
            return false;
        }
        return matches(needle, pos);
    }
    
    final private boolean matches(Buffer needle, int pos) {
        byte[] data = this.data;
        int offset = this.offset;
        int needleLength = needle.length;
        byte[] needleData = needle.data;
        int needleOffset = needle.offset;
        
        for (int i = 0; i < needleLength; i++) {
            if( data[offset + pos+ i] != needleData[needleOffset + i] ) {
                return false;
            }
        }
        return true;
    }

    
    final public Buffer trim() {
        return trimFront().trimEnd();
    }

    final public Buffer trimEnd() {
        byte data[] = this.data;
        int offset = this.offset;
        int length = this.length;
        int end = offset+this.length-1;
        int pos = end;
        
        while ((offset <= pos) && (data[pos] <= ' ')) {
            pos--;
        }
        return (pos == end) ? this : new Buffer(data, offset, (length-(end-pos))); 
    }

    final public Buffer trimFront() {
        byte data[] = this.data;
        int offset = this.offset;
        int end = offset+this.length;
        int pos = offset;
        while ((pos < end) && (data[pos] <= ' ')) {
            pos++;
        }
        return (pos == offset) ? this : new Buffer(data, pos, (length-(pos-offset))); 
    }

    final public Buffer buffer() {
        return new Buffer(this);
    }
    final public AsciiBuffer ascii() {
        return new AsciiBuffer(this);
    }
    final public  UTF8Buffer utf8() {
        return new UTF8Buffer(this);
    }
    
    final public Buffer[] split(byte separator) {
        ArrayList<Buffer> rc = new ArrayList<Buffer>();
        
        byte data[] = this.data;
        int pos = this.offset;
        int nextStart = pos;
        int end = pos+this.length;

        while( pos < end ) {
            if( data[pos]==separator ) {
                if( nextStart < pos ) {
                    rc.add(new Buffer(data, nextStart, (pos-nextStart)));
                }
                nextStart = pos+1;
            }
            pos++;
        }
        if( nextStart < pos ) {
            rc.add(new Buffer(data, nextStart, (pos-nextStart)));
        }

        return rc.toArray(new Buffer[rc.size()]);
    }

    public void reset() {
        offset = 0;
        length = data.length;
    }

    ///////////////////////////////////////////////////////////////////
    // Overrides
    ///////////////////////////////////////////////////////////////////
    
    @Override
    public int hashCode() {
        byte[] data = this.data;
        int offset = this.offset;
        int length = this.length;

        byte[] target = new byte[4];
        for (int i = 0; i < length; i++) {
            target[i % 4] ^= data[offset + i];
        }
        return target[0] << 24 | target[1] << 16 | target[2] << 8 | target[3];
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj == null || obj.getClass() != Buffer.class)
            return false;

        return equals((Buffer) obj);
    }

    public int compareTo(Buffer o) {
        if( this == o )
            return 0;
        
        byte[] data = this.data;
        int offset = this.offset;
        int length = this.length;
        
        int oLength = o.length;
        int oOffset = o.offset;
        byte[] oData = o.data;
        
        int minLength = Math.min(length, oLength);
        if (offset == oOffset) {
            int pos = offset;
            int limit = minLength + offset;
            while (pos < limit) {
                int b1 = 0xFF & data[pos];
                int b2 = 0xFF & oData[pos];
                if (b1 != b2) {
                    return b1 - b2;
                }
                pos++;
            }
        } else {
            int offset1 = offset;
            int offset2 = oOffset;
            while ( minLength-- != 0) {
                int b1 = 0xFF & data[offset1++];
                int b2 = 0xFF & oData[offset2++];
                if (b1 != b2) {
                    return b1 - b2;
                }
            }
        }
        return length - oLength;
    }

    /**
     * same as out.write(data, offset, length);
     */
    public void writeTo(DataOutput out) throws IOException {
        out.write(data, offset, length);
    }

    /**
     * same as out.write(data, offset, length);
     */
    public void writeTo(OutputStream out) throws IOException {
        out.write(data, offset, length);
    }

    /**
     * same as in.readFully(data, offset, length);
     */
    public void readFrom(DataInput in) throws IOException {
        in.readFully(data, offset, length);
    }

    /**
     * same as in.read(data, offset, length);
     */
    public int readFrom(InputStream in) throws IOException {
        return in.read(data, offset, length);
    }

    ///////////////////////////////////////////////////////////////////
    // Statics
    ///////////////////////////////////////////////////////////////////
    
    public static String string(Buffer value) {
        if( value==null ) {
            return null;
        }
        return value.toString();
    }

    final public static Buffer join(List<Buffer> items, Buffer seperator) {
        if (items.isEmpty())
            return new Buffer(seperator.data, 0, 0);

        int size = 0;
        for (Buffer item : items) {
            size += item.length;
        }
        size += seperator.length * (items.size() - 1);

        int pos = 0;
        byte data[] = new byte[size];
        for (Buffer item : items) {
            if (pos != 0) {
                System.arraycopy(seperator.data, seperator.offset, data, pos, seperator.length);
                pos += seperator.length;
            }
            System.arraycopy(item.data, item.offset, data, pos, item.length);
            pos += item.length;
        }

        return new Buffer(data, 0, size);
    }


    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(data, offset, length);
    }

    public static AsciiBuffer ascii(String value) {
        return AsciiBuffer.ascii(value);
    }
    public static AsciiBuffer ascii(Buffer buffer) {
        return AsciiBuffer.ascii(buffer);
    }

    public static UTF8Buffer utf8(String value) {
        return UTF8Buffer.utf8(value);
    }
    public static UTF8Buffer utf8(Buffer buffer) {
        return UTF8Buffer.utf8(buffer);
    }

    @Override
    public String toString() {
        int size = length;
        boolean asciiPrintable = true;
        for( int i=0; i < size; i++ ) {
            int c = data[offset+i] & 0xFF;
            if( c > 126 || c < 32 ) { // not a printable char
                if( !(c=='\n' || c=='\r' | c=='\n' | c==27) ) { // except these.
                    asciiPrintable = false;
                    break;
                }
            }
        }
        if( asciiPrintable ) {
            return "ascii: "+ascii();
        } else {
            return "hex: "+HexSupport.toHexFromBuffer(this);
        }
    }

}
