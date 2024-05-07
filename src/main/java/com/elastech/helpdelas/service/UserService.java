package com.elastech.helpdelas.service;

import com.elastech.helpdelas.dtos.SectorDTO;
import com.elastech.helpdelas.dtos.UserDTO;
import com.elastech.helpdelas.model.RoleModel;
import com.elastech.helpdelas.model.SectorModel;
import com.elastech.helpdelas.model.UserModel;
import com.elastech.helpdelas.repositories.RoleRepository;
import com.elastech.helpdelas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private SectorService sectorService;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public UserDTO salvar(UserDTO userDTO) throws Exception {
        Optional<UserModel> byEmail = userRepository.findByEmail(userDTO.getEmail());
        if(!byEmail.isPresent()){
            RoleModel role = roleRepository.findByName(RoleModel.Values.USER.name());
            userDTO.setRole(role);
            userDTO.setSector(userDTO.getSector());
            userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            UserModel userModel = UserDTO.convert(userDTO);
            userRepository.save(userModel);
            return new UserDTO(userModel);
        } else {
            throw new Exception("Já existe um cliente cadastrado com esse e-mail.");
        }
    }

    public List<SectorDTO> findAllSector(){
        return sectorService.findAll();
    }

    public List<UserModel> userModelList(UserModel filtroUser){
        //metodo que permite buscar por todos os dados da base da table especifico bem como filtrar por qualquer coluna que contenha  na
        //base de dados, ignorando letras maiuscula/minuscula
        ExampleMatcher matcher = ExampleMatcher
                .matching()
                .withIgnoreCase()
                .withStringMatcher(
                        ExampleMatcher.StringMatcher.CONTAINING);

        Example example = Example.of(filtroUser, matcher); //pegar as propriedas populadas e criar o objeto
        return userRepository.findAll(example);
    }

    public UserDTO updateUserById(Long id, UserDTO userDTO) throws Exception {
        Optional<UserModel> userExistente =  userRepository.findById(id);
        if(userExistente.isPresent()){
            UserModel user = userExistente.get();
            user.setEmail(userDTO.getEmail());
            user.setName(userDTO.getName());
            user.setSector(userDTO.getSector());
            user.setSupervisor(userDTO.getSupervisor());
            userRepository.save(user);
            return new UserDTO(user);
        } else {
            return userDTO;
        }
    }

    public UserDTO getUserByEmail(String email){
        Optional<UserModel> user = userRepository.findByEmail(email);
        if(user.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Usuário não encontrado");
        }
        return new UserDTO(user.get());
    }

}
