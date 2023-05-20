package pri.prepare.lovehymn.server.function;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import com.google.common.io.ByteSource;

public class GzippedByteSource extends ByteSource {

    private final ByteSource source;

    public GzippedByteSource(ByteSource gzippedSource) {
        source = gzippedSource;
    }

    @Override
    public InputStream openStream() throws IOException {
        return new GZIPInputStream(source.openStream());
    }
}
