package pl.poznan.put.rnatangoengine.database.converters;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import pl.poznan.put.rnatangoengine.dto.Model;

@Converter
public class ModelListConverter implements AttributeConverter<List<Model>, String> {

  @Override
  public String convertToDatabaseColumn(List<Model> modelList) {
    return modelList != null ? new Gson().toJson(modelList) : "";
  }

  @Override
  public List<Model> convertToEntityAttribute(String string) {
    return string != null
        ? new Gson().fromJson(string, new ArrayList<Model>() {}.getClass())
        : new ArrayList<Model>();
  }
}
