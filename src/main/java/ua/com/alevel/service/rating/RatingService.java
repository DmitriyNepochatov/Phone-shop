package ua.com.alevel.service.rating;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.com.alevel.repository.rating.RatingRepository;

@Service
public class RatingService {
    private final RatingRepository ratingRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(RatingService.class);

    @Autowired
    public RatingService(RatingRepository ratingRepository) {
        this.ratingRepository = ratingRepository;
    }

    @Transactional
    public void updateRating(int numberOfPoints, int totalPoints, String id) {
        ratingRepository.updateRating(numberOfPoints, totalPoints, id);
        LOGGER.info("Rating {} updated", id);
    }
}
