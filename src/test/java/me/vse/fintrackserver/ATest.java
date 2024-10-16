package me.vse.fintrackserver;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.MockitoAnnotations;

public abstract class ATest {

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

}
