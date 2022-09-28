package com.amazon.ata.advertising.service.businesslogic;

import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.model.AdvertisementContent;
import com.amazon.ata.advertising.service.model.EmptyGeneratedAdvertisement;
import com.amazon.ata.advertising.service.model.GeneratedAdvertisement;
import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.TargetingEvaluator;
import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import javax.inject.Inject;

/**k
 * This class is responsible for picking the advertisement to be rendered.
 */
public class AdvertisementSelectionLogic {

    private static final Logger LOG = LogManager.getLogger(AdvertisementSelectionLogic.class);

    private final ReadableDao<String, List<AdvertisementContent>> contentDao;
    private final ReadableDao<String, List<TargetingGroup>> targetingGroupDao;
    private Random random = new Random();

    /**
     * Constructor for AdvertisementSelectionLogic.
     * @param contentDao Source of advertising content.
     * @param targetingGroupDao Source of targeting groups for each advertising content.
     */
    @Inject
    public AdvertisementSelectionLogic(ReadableDao<String, List<AdvertisementContent>> contentDao,
                                       ReadableDao<String, List<TargetingGroup>> targetingGroupDao) {
        this.contentDao = contentDao;
        this.targetingGroupDao = targetingGroupDao;
    }

    /**
     * Setter for Random class.
     * @param random generates random number used to select advertisements.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Gets all of the content and metadata for the marketplace and determines which content can be shown.  Returns the
     * eligible content with the highest click through rate.  If no advertisement is available or eligible, returns an
     * EmptyGeneratedAdvertisement.
     *
     * @param customerId - the customer to generate a custom advertisement for
     * @param marketplaceId - the id of the marketplace the advertisement will be rendered on
     * @return an advertisement customized for the customer id provided, or an empty advertisement if one could
     *     not be generated.
     */
    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) {
        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
        TargetingEvaluator evaluator = new TargetingEvaluator(new RequestContext(customerId, marketplaceId));

        Comparator<Double> clickThroughRateComparator = Comparator.reverseOrder();
        Map<Double, AdvertisementContent> clickThroughRateMap = new TreeMap<>(clickThroughRateComparator);

        if (StringUtils.isEmpty(marketplaceId)) {
            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
        } else {
            Map<Double, AdvertisementContent> advertisementContentTreeMap = new TreeMap<>(Comparator.reverseOrder());
            contentDao.get(marketplaceId)
                    .forEach(adContent -> (targetingGroupDao.get(adContent.getContentId())
                            .stream()
                            .filter(targetingGroup -> evaluator
                            .evaluate(targetingGroup).isTrue()))
                            .forEach(x -> advertisementContentTreeMap.put(x.getClickThroughRate(), adContent)));
            generatedAdvertisement = advertisementContentTreeMap.values()
                    .stream()
                    .findFirst()
                    .map(GeneratedAdvertisement::new)
                    .orElseGet(EmptyGeneratedAdvertisement::new);

//            final List<AdvertisementContent> contents = contentDao.get(marketplaceId);
//
//            final List<TargetingGroup> groups = new ArrayList<>();
//            for (AdvertisementContent content : contents) {
//                for (TargetingGroup group : targetingGroupDao.get(content.getContentId())) {
//                    if (evaluator.evaluate(group).equals(TargetingPredicateResult.TRUE)) {
//                        groups.add(group);
//                    }
//                }
//                }
//
//            final List<AdvertisementContent> contentList = new ArrayList<>();
//            for (TargetingGroup group : groups) {
//                for (AdvertisementContent content : contents) {
//                    if (content.getContentId().equals(group.getContentId())) {
//                        contentList.add(content);
//                    }
//                }
//            }
//
//            if (CollectionUtils.isNotEmpty(contentList)) {
//                AdvertisementContent randomAdvertisementContent = contentList.get(random.nextInt(contentList.size()));
//                generatedAdvertisement = new GeneratedAdvertisement(randomAdvertisementContent);
//            }

        }
        return generatedAdvertisement;
    }
}
