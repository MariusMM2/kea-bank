package exam.marius.keabank.model;

import java.io.Serializable;
import java.util.UUID;

public interface DatabaseItem extends Serializable {

    UUID getId();

    boolean equals(Object o);
}
