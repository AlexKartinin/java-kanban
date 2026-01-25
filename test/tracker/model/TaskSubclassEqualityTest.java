package tracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskSubclassEqualityTest {

    @Test
    void epicsShouldBeEqualIfIdsAreEqual() {
        Epic e1 = new Epic(1, "E1", "d1");
        Epic e2 = new Epic(1, "E2", "d2");

        assertEquals(e1, e2, "Два Epic должны быть равны, если равен их id");
        assertEquals(e1.hashCode(), e2.hashCode(), "hashCode Epic должен совпадать при равных id");
    }

    @Test
    void subtasksShouldBeEqualIfIdsAreEqual() {
        Epic epic = new Epic(100, "Epic", "d");
        Subtask s1 = new Subtask(5, "S1", "d1", epic);
        Subtask s2 = new Subtask(5, "S2", "d2", epic);

        assertEquals(s1, s2, "Две Subtask должны быть равны, если равен их id");
        assertEquals(s1.hashCode(), s2.hashCode(), "hashCode Subtask должен совпадать при равных id");
    }
}
