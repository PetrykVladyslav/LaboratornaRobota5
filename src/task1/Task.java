package task1;

import java.io.Serializable;

interface Task extends Serializable {
    Result execute();
}