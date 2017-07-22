package kuona.maven.analyser.model;

import com.google.gson.Gson;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ElementTest {
    @Test
    public void ElementsSerialize() {

        assertThat(new Gson().toJson(new FlareNode("Foo")).toString(), is("{\"name\":\"Foo\",\"children\":[]}"));
    }
}
