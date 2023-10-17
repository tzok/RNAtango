package pl.poznan.put.rnatangoengine.database.converters;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import pl.poznan.put.rnatangoengine.dto.Selection;

@Converter
public class SelectionListConverter implements AttributeConverter<List<Selection>, String> {

  @Override
  public String convertToDatabaseColumn(List<Selection> selectionList) {
    return selectionList != null ? new Gson().toJson(selectionList) : "";
  }

  @Override
  public List<Selection> convertToEntityAttribute(String string) {
    return string != null
        ? new Gson().fromJson(string, new ArrayList<Selection>() {}.getClass())
        : new ArrayList<Selection>();
  }
}
