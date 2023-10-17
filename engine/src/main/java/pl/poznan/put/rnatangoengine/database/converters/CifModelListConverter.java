package pl.poznan.put.rnatangoengine.database.converters;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import pl.poznan.put.pdb.analysis.CifModel;

@Converter
public class CifModelListConverter implements AttributeConverter<List<CifModel>, String> {

  @Override
  public String convertToDatabaseColumn(List<CifModel> modelList) {
    return modelList != null ? new Gson().toJson(modelList) : "";
  }

  @Override
  public List<CifModel> convertToEntityAttribute(String string) {
    return string != null
        ? new Gson().fromJson(string, new ArrayList<CifModel>() {}.getClass())
        : new ArrayList<CifModel>();
  }
}
