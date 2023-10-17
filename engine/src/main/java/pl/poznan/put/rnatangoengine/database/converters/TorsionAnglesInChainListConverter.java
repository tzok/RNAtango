package pl.poznan.put.rnatangoengine.database.converters;

import com.google.gson.Gson;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.ArrayList;
import java.util.List;
import pl.poznan.put.rnatangoengine.dto.TorsionAnglesInChain;

@Converter
public class TorsionAnglesInChainListConverter
    implements AttributeConverter<List<TorsionAnglesInChain>, String> {

  @Override
  public String convertToDatabaseColumn(List<TorsionAnglesInChain> torsionAnglesInChainList) {
    return torsionAnglesInChainList != null ? new Gson().toJson(torsionAnglesInChainList) : "";
  }

  @Override
  public List<TorsionAnglesInChain> convertToEntityAttribute(String string) {
    return string != null
        ? new Gson().fromJson(string, new ArrayList<TorsionAnglesInChain>() {}.getClass())
        : new ArrayList<TorsionAnglesInChain>();
  }
}
