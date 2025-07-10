package server.database;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.data.User;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserRepository {
        private final Path file = Paths.get("src/server/database/data/users.json");
        private final ObjectMapper mapper = new ObjectMapper();

    public void editUserAndWrite(User updatedItem) throws IOException {
        List<User> items = getUsersFromFile();

        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(items.get(i).getName(), updatedItem.getName())) {
                items.set(i, updatedItem);
                break;
            }
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
    }

        public List<User> getUsersFromFile() throws IOException {
            if(!file.toFile().exists()) {
                return new ArrayList<>();
            }

            return mapper.readValue(file.toFile(), new TypeReference<List<User>>() {});
        }

        public void writeUserToFile(User item) throws IOException {
            List<User> items = this.getUsersFromFile();
            items.add(item);


            mapper.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), items);
        }
    }
