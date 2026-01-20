package com.tcaputi.back.custody.client.infrastructure;

import com.tcaputi.back.custody.client.domain.model.Client;
import com.tcaputi.back.custody.client.domain.model.embedded.Address;
import com.tcaputi.back.custody.client.interfaces.dto.AddressDto;
import com.tcaputi.back.custody.client.interfaces.dto.ClientDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {

    @Mapping(source = "billingAddress", target = "billingAddress")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    Client toEntity(ClientDto dto);

    @Mapping(source = "billingAddress", target = "billingAddress")
    @Mapping(source = "shippingAddress", target = "shippingAddress")
    ClientDto toDto(Client entity);

    // MapStruct utilisera ces méthodes pour les champs imbriqués
    default Address addressDtoToAddress(AddressDto dto) {
        if (dto == null) return null;
        Address address = new Address();
        address.setStreet(dto.street());
        address.setZip(dto.zip());
        address.setCity(dto.city());
        address.setCountry(dto.country());
        return address;
    }

    default AddressDto addressToAddressDto(Address entity) {
        if (entity == null) return null;
        return new AddressDto(
                entity.getStreet(),
                entity.getZip(),
                entity.getCity(),
                entity.getCountry()
        );
    }
}
