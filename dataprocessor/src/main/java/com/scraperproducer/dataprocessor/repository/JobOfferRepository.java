package com.scraperproducer.dataprocessor.repository;

import com.scraperproducer.dataprocessor.model.JobOffer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobOfferRepository extends CrudRepository<JobOffer, Long> {
    Optional<JobOffer> findFirstByDedupeKey(String dedupeKey);

    Optional<JobOffer> findFirstBySourceUrl(String sourceUrl);

    Optional<JobOffer> findFirstByTitleAndCompanyAndLocation(String title, String company, String location);
}
