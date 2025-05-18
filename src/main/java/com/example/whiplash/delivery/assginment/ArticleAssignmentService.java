package com.example.whiplash.delivery.assginment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ArticleAssignmentService {
    private final ArticleAssigner assigner;

    @Transactional
    public void assign() {

    }
}
