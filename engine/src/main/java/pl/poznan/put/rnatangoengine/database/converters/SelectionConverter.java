package pl.poznan.put.rnatangoengine.database.converters;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import pl.poznan.put.rnatangoengine.dto.Selection;

@Converter
public class SelectionConverter implements AttributeConverter<Selection, String> {

  @Override
  public String convertToDatabaseColumn(Selection selection) {
    return selection != null ? new Gson().toJson(selection) : "";
  }

  @Override
  public Selection convertToEntityAttribute(String string) {
    return string != null ? new Gson().fromJson(string, Selection.class) : null;
  }
}
