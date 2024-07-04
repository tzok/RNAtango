package pl.poznan.put.rnatangoengine.database.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import pl.poznan.put.rnatangoengine.dto.Angle;

@Converter
public class AngleListConverter implements AttributeConverter<List<Angle>, String> {

  private static final String SEPARATOR = ",";

  @Override
  public String convertToDatabaseColumn(List<Angle> attribute) {
    if (attribute == null || attribute.isEmpty()) {
      return "";
    }
    return attribute.stream().map(Enum::name).collect(Collectors.joining(SEPARATOR));
  }

  @Override
  public List<Angle> convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
      return Arrays.asList();
    }
    return Arrays.stream(dbData.split(SEPARATOR))
        .map(String::toUpperCase)
        .map(Angle::valueOf)
        .collect(Collectors.toList());
  }
}
