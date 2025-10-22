package casualgames.userservice.service;

import java.util.List;
import java.util.Optional;

public interface Service<T, ID> {

    T findById(ID id);

    List<T> findAll();

    void delete (ID id);

}
