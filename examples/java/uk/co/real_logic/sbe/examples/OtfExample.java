package uk.co.real_logic.sbe.examples;

import baseline.Car;
import baseline.MessageHeader;
import uk.co.real_logic.sbe.codec.java.DirectBuffer;
import uk.co.real_logic.sbe.ir.Decoder;
import uk.co.real_logic.sbe.ir.Encoder;
import uk.co.real_logic.sbe.ir.IntermediateRepresentation;
import uk.co.real_logic.sbe.xml.IrGenerator;
import uk.co.real_logic.sbe.xml.MessageSchema;
import uk.co.real_logic.sbe.xml.XmlSchemaParser;

import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class OtfExample
{
    private static final MessageHeader MESSAGE_HEADER = new MessageHeader();
    private static final Car CAR = new Car();
    private static final int ACTING_VERSION = 0;
    private static final int MSG_BUFFER_CAPACITY = 16 * 1024;
    private static final int SCHEMA_BUFFER_CAPACITY = 16 * 1024;

    public static void main(final String[] args) throws Exception
    {
        // Encode up message and schema as if we just got them off the wire.
        final ByteBuffer encodedSchemaBuffer = ByteBuffer.allocateDirect(SCHEMA_BUFFER_CAPACITY);
        encodeSchema(encodedSchemaBuffer);

        final ByteBuffer encodedMsgBuffer = ByteBuffer.allocateDirect(MSG_BUFFER_CAPACITY);
        encodeTestMessage(encodedMsgBuffer);


        // Now lets decode the schema IR so we have IR objects.
        encodedSchemaBuffer.flip();
        final IntermediateRepresentation ir = decodeIr(encodedSchemaBuffer);

        // Now we have IR we can read the message header

        // Given the message header we can select the appropriate message to decode.
    }

    private static void encodeSchema(final ByteBuffer buffer)
        throws Exception
    {
        try (final InputStream in = new FileInputStream("examples/resources/TestSchema.xml"))
        {
            final MessageSchema schema = XmlSchemaParser.parse(in);
            final IntermediateRepresentation ir = new IrGenerator().generate(schema);
            new Encoder(buffer, ir).encode();
        }
    }

    private static void encodeTestMessage(final ByteBuffer buffer)
    {
        final DirectBuffer directBuffer = new DirectBuffer(buffer);

        int bufferOffset = 0;
        MESSAGE_HEADER.wrap(directBuffer, bufferOffset, ACTING_VERSION)
                      .blockLength(CAR.blockLength())
                      .templateId(CAR.templateId())
                      .version(CAR.templateVersion());

        bufferOffset += MESSAGE_HEADER.size();

        GeneratedStubExample.encode(CAR, directBuffer, bufferOffset);
    }

    private static IntermediateRepresentation decodeIr(final ByteBuffer buffer)
        throws IOException
    {
        final Decoder decoder = new Decoder(buffer);
        return decoder.decode();
    }
}
