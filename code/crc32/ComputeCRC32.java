
import java.nio.ByteBuffer;
import java.util.zip.Checksum;
import java.util.zip.CRC32;

public class ComputeCRC32 {

  public static void main(String[] args) {
    int i = 0;
    var n = Integer.parseInt(args[i++]);
    var bs = args[i++].getBytes();

    var bx = new byte[bs.length+4];
    System.arraycopy(bs,0, bx,4, bs.length);
    iterateHash(n, bx);
  }

  static void iterateHash(int n, byte[] bx) {
    var bb = ByteBuffer.wrap(bx);
    long s = 0;
    for (int i = 0; i < n; i++) {
      bb.putInt(0, i);
      var h = hash(bx);
      s += h;
    }
    System.err.println(s);
  }

  static long hash(byte[] d) {
    Checksum h = new CRC32();
    h.update(d);
    return h.getValue();
  }
}
