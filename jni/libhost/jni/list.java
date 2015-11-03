import java.io.*;

public class list {
    private static char nibble(int c) {
        return (char)(c < 10 ? ('0' + c) : ('a' + (c-10)));
    }
    public static void main(String[] argv)
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(100);
        OutputStreamWriter writer = null;
        try {
            writer = new OutputStreamWriter(stream, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
        }

        int n = Integer.parseInt(argv[1], 16);
        try {
            writer.write(n);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }

        byte[] array = stream.toByteArray();

        System.out.print("        case '" + argv[0] + "':   return \"");
        for (int i=0; i<array.length; i++) {
            int b = array[i];
            System.out.print("\\x" + nibble((b >> 4) & 0x0f) + nibble(b & 0xf));
        }
        System.out.println("\";");
    }
}

