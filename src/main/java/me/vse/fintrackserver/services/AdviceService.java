package me.vse.fintrackserver.services;

import me.vse.fintrackserver.advice.ExpenseByCategoryCheck;
import me.vse.fintrackserver.advice.ExpenseCheck;
import me.vse.fintrackserver.repositories.AccountRepository;
import me.vse.fintrackserver.rest.responses.AdviceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class AdviceService {

    private final Set<AdviceResponse> responsesToBeSent = new HashSet<>();

//    @Scheduled(cron = "0 0 3 * * ?")
    @Scheduled(fixedRate = 3000)
    public void scheduleTask() {
        responsesToBeSent.clear();

        Thread thread = new Thread(){
            public void run() {
                performAdviceChecks();

                this.interrupt();
            }
        };
        thread.start();
    }

    @Autowired
    ExpenseByCategoryCheck expenseByCategoryCheck;
    @Autowired
    ExpenseCheck expenseCheck;

    private void performAdviceChecks() {

        expenseByCategoryCheck.perform();

    }

    public void addResponse(AdviceResponse response) {
        responsesToBeSent.add(response);
    }

    public AdviceResponse getAdvice(String userId) {
        AdviceResponse response = null;

        for (AdviceResponse currentResponse : responsesToBeSent) {
            if (currentResponse.getUserId().equals(userId)) {
                response = response == null ? currentResponse : response;
                responsesToBeSent.remove(currentResponse);
            }
        }

        return response;
    }


}
