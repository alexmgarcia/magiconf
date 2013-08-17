package serializers_deserializers;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import tables.NotPresentedPublication;

/**
 * @author Alexandre
 *
 * JSON Serializer to the Not Presented Publication
 */
public class NotPresentedPublicationDeserializer extends JsonDeserializer<NotPresentedPublication> {

	@Override
	public NotPresentedPublication deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException, JsonProcessingException {
		ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return new NotPresentedPublication(node.get("objects").get(0).get("title").getTextValue(), node.get("objects").get(0).get("abstract").getTextValue(), node.get("objects").get(0).get("authors").getTextValue());
	}

}
