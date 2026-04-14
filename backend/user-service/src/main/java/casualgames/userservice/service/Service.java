package casualgames.userservice.service;

import java.util.List;

public interface Service<T, ID> {

    List<T> findAll();
}
