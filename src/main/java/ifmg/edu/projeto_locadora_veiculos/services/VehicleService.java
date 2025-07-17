package ifmg.edu.projeto_locadora_veiculos.services;

import ifmg.edu.projeto_locadora_veiculos.dto.VehicleDTO;
import ifmg.edu.projeto_locadora_veiculos.entities.Vehicle;
import ifmg.edu.projeto_locadora_veiculos.repositories.VehicleRepository;
import ifmg.edu.projeto_locadora_veiculos.services.exceptions.DatabaseException;
import ifmg.edu.projeto_locadora_veiculos.services.exceptions.ResourceNotFound;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Page<VehicleDTO> findAll(Pageable pageable) {
        return vehicleRepository.findAll(pageable).map(VehicleDTO::new);
    }

    @Transactional(readOnly = true)
    public VehicleDTO findById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFound("Veículo não encontrado"));
        return new VehicleDTO(vehicle);
    }

    @Transactional
    public VehicleDTO insert(VehicleDTO dto) {
        Vehicle entity = new Vehicle();
        copyDtoToEntity(dto, entity);
        entity = vehicleRepository.save(entity);
        return new VehicleDTO(entity);
    }

    @Transactional
    public VehicleDTO update(Long id, VehicleDTO dto) {
        try {
            Vehicle entity = vehicleRepository.getReferenceById(id);
            copyDtoToEntity(dto, entity);
            entity = vehicleRepository.save(entity);
            return new VehicleDTO(entity);
        } catch (EntityNotFoundException e) {
            throw new ResourceNotFound("Veículo não encontrado: " + id);
        }
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public void delete(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new ResourceNotFound("Veículo não encontrado - ID: " + id);
        }
        try {
            vehicleRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            throw new DatabaseException("Integridade violada");
        }
    }

    private void copyDtoToEntity(VehicleDTO dto, Vehicle entity) {
        entity.setPlate(dto.getPlate());
        entity.setBrand(dto.getBrand());
        entity.setModel(dto.getModel());
        entity.setYear(dto.getYear());
        entity.setColor(dto.getColor());
        entity.setDescription(dto.getDescription());
        entity.setImgUrl(dto.getImgUrl());
        entity.setDailyValue(dto.getDailyValue());
    }

    public List<String> vehicleList() {
        List<Vehicle> vehicles = vehicleRepository.findAll();

        return vehicles.stream()
                .map(v -> String.format(
                        "Carro - Placa: %s  - Modelo: %s - Marca: %s - Cor: %s - Ano: %s",
                        safe(v.getPlate()),
                        safe(v.getModel()),
                        safe(v.getBrand()),
                        safe(v.getColor()),
                        safe(v.getYear())
                ))
                .toList();
    }

    private String safe(String value) {
        return value == null ? "N/A" : value;
    }
}

