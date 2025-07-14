package ifmg.edu.projeto_locadora_veiculos.dto;

import ifmg.edu.projeto_locadora_veiculos.entities.Vehicle;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.hateoas.RepresentationModel;

import java.util.Objects;

public class VehicleDTO extends RepresentationModel<VehicleDTO> {

    @Schema(description = "ID do veículo gerado pelo banco de dados")
    private long id;

    @Schema(description = "Placa do veículo")
    @NotBlank(message = "A placa é obrigatória")
    private String plate;

    @Schema(description = "Marca do veículo")
    @NotBlank(message = "A marca é obrigatória")
    private String brand;

    @Schema(description = "Modelo do veículo")
    @NotBlank(message = "O modelo é obrigatório")
    private String model;

    @Schema(description = "Ano do veículo")
    @NotBlank(message = "O ano é obrigatório")
    private String year;

    @Schema(description = "Cor do veículo")
    private String color;

    @Schema(description = "Descrição do veículo")
    private String description;

    @Schema(description = "URL da imagem do veículo")
    private String imgUrl;

    @Schema(description = "Valor da diária do veículo")
    @Positive(message = "O valor da diária deve ser positivo")
    private double dailyValue;

    public VehicleDTO() {}

    public VehicleDTO(String plate, String brand, String model, String year, String color,
                      String description, String imgUrl, double dailyValue) {
        this.plate = plate;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.description = description;
        this.imgUrl = imgUrl;
        this.dailyValue = dailyValue;
    }

    public VehicleDTO(Vehicle vehicle) {
        this.id = vehicle.getId();
        this.plate = vehicle.getPlate();
        this.brand = vehicle.getBrand();
        this.model = vehicle.getModel();
        this.year = vehicle.getYear();
        this.color = vehicle.getColor();
        this.description = vehicle.getDescription();
        this.imgUrl = vehicle.getImgUrl();
        this.dailyValue = vehicle.getDailyValue();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public double getDailyValue() {
        return dailyValue;
    }

    public void setDailyValue(double dailyValue) {
        this.dailyValue = dailyValue;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof VehicleDTO dto)) return false;
        return id == dto.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "VehicleDTO{" +
                "id=" + id +
                ", plate='" + plate + '\'' +
                ", brand='" + brand + '\'' +
                ", model='" + model + '\'' +
                ", year='" + year + '\'' +
                ", color='" + color + '\'' +
                ", description='" + description + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", dailyValue=" + dailyValue +
                '}';
    }
}