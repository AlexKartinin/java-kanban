package tracker.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskEqualityTest {

    @Test
    void tasksShouldBeEqualIfIdsAreEqual() {
        Task t1 = new Task(10, "A", "desc");
        Task t2 = new Task(10, "B", "another desc");

        assertEquals(t1, t2, "Две Task должны быть равны, если равен их id");
        assertEquals(t1.hashCode(), t2.hashCode(), "hashCode должен совпадать при равных id");
    }

    @Test
    void tasksShouldNotBeEqualIfIdsAreDifferent() {
        Task t1 = new Task(10, "A", "desc");
        Task t2 = new Task(11, "A", "desc");

        assertNotEquals(t1, t2, "Task не должны быть равны при разных id");
    }
}
