package app.daos;

import app.exceptions.ApiException; // ✅ BRUG DENNE!
import java.util.List;

public interface IDAO<T, ID> {
    T read(ID id) throws ApiException;
    List<T> readAll() throws ApiException;
    T create(T dto) throws ApiException;
    T update(ID id, T dto) throws ApiException;
    void delete(ID id, dk.bugelhartmann.UserDTO user) throws ApiException;
}
