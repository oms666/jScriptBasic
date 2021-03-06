package com.scriptbasic.sourceproviders;

import com.scriptbasic.readers.SourceReader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TestSingleIncludeSourcePathSourceProvider {

    @Test
    public void testFSPFileNotFound() throws IOException {
        final var tsispsp = new TestedSingleIncludeSourcePathSourceProvider();
        tsispsp.setSingleInclude(null);
        //noinspection ResultOfMethodCallIgnored
        tsispsp.getSingleInclude();
        tsispsp.get("habakukk");
        tsispsp.get("habakukk", "kakukk");
        tsispsp.setSingleInclude(new BasicSingleIncludeChecker());
        tsispsp.get("habakukk", "kakukk");
    }

    private class TestedSingleIncludeSourcePathSourceProvider extends
            AbstractSingleIncludeSourcePathSourceProvider {

        @Override
        protected SourceReader getSource(final String sourceName) {
            return null;
        }

        @Override
        protected SourceReader getSource(final String sourceName,
                                         final String referencingSource) {
            return null;
        }

        @Override
        protected String getKeyName(final String sourceName) {
            return null;
        }

        @Override
        protected String getKeyName(final String sourceName,
                                    final String referencingSource) {
            return null;
        }

    }
}
