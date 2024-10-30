package me.vse.fintrackserver.advice;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;
import me.vse.fintrackserver.enums.Frequencies;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.rest.responses.AdviceResponse;


@Getter
@Setter
public abstract class Advice {

    protected AccountRepository accountRepository;
    protected Frequencies frequency;

    public void perform() {

    }

}
