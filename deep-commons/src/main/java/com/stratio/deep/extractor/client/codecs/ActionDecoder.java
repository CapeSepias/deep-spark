/*
 * Copyright 2012 The Netty Project
 * 
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.stratio.deep.extractor.client.codecs;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.stratio.deep.extractor.actions.Action;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.List;

public class ActionDecoder extends ByteToMessageDecoder {


    private final Kryo kryo;
    private final Input input = new Input();
    private int length = -1;

    public ActionDecoder (Kryo kryo) {
        this.kryo = kryo;
    }



    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {

/*
        input.setBuffer(in.array(), in.readerIndex(), in.readableBytes());
        if (length == -1) {
            // Read length.
            if (in.readableBytes() < 4) return;
            length = input.readInt();
            in.readerIndex(input.position());
        }

        if (in.readableBytes() < length) return;

        length = -1;
        Object object = kryo.readClassAndObject(input);
        in.readerIndex(input.position());

        out.add((Action) object);
*/

                byte[] decoded = null;


        if (length == -1) {
            // Read length.
            if (in.readableBytes() < 4) return;
            length = in.readInt();
        }

        if (in.readableBytes() < length) return;
        decoded = new byte[length];
        length = -1;

        in.readBytes(decoded);
        input.setBuffer(decoded);
        Object object = kryo.readClassAndObject(input);
        in.readerIndex(input.position()+4);

        out.add((Action) object);

        int dummy = in.readableBytes();

/*
        byte[] decoded = null;


        if (length == -1) {
            // Read length.
            if (in.readableBytes() < 4) return;
            length = in.readInt();
        }

        if (in.readableBytes() < length){
            return;
        }
        decoded = new byte[length];
        length = -1;

        in.readBytes(decoded);
        input.setBuffer(decoded,0,decoded.length);

        Object object = kryo.readClassAndObject(input);

        out.add((Action) object);


        int dummy = in.readableBytes();
        in.skipBytes(dummy);
*/

/*        // Wait until the length prefix is available.
        if (in.readableBytes() < 5) {
            return;
        }

        // Wait until the whole data is available.
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }

        // Convert the received data into a new BigInteger.
        byte[] decoded = new byte[dataLength];
        in.readBytes(decoded);

        ByteArrayInputStream bis = new ByteArrayInputStream(decoded);
        ObjectInput inObj = null;
        Action action = null;
        try {
            inObj = new ObjectInputStream(bis);
            action = (Action) inObj.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                bis.close();
            } catch (IOException ex) {
                // ignore close exception
            }
            try {
                if (inObj != null) {
                    inObj.close();
                }
            } catch (IOException ex) {
                // ignore close exception
            }
        }

        out.add(action);
        */
    }
}