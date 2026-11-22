package me.ayydxn.niagara.codegen;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

public final class GoldenFiles
{
    private GoldenFiles()
    {
    }

    public static void assertMatch(String directory, Map<String, String> files) throws IOException
    {
        for (Map.Entry<String, String> file : files.entrySet())
        {
            String expected = read("/golden/" + directory + "/" + file.getKey() + ".golden");

            if (expected != null && withoutTimestamp(file.getValue()).equals(withoutTimestamp(expected)))
                continue;

            Path actual = Path.of("build", "golden-actual", directory, file.getKey());
            Files.createDirectories(actual.getParent());
            Files.writeString(actual, file.getValue());

            fail((expected == null ? "No golden file for " : "Generated output differs from the golden file for ") + file.getKey()
                    + ". The generated version is in " + actual.toAbsolutePath());
        }
    }

    private static String withoutTimestamp(String content)
    {
        return content.replaceAll("(?m)^// Generated on .*\\R", "");
    }

    private static String read(String resource) throws IOException
    {
        try (InputStream stream = GoldenFiles.class.getResourceAsStream(resource))
        {
            return stream == null ? null : new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        }
    }
}
