package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;

    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */
    public TargetingEvaluator(RequestContext requestContext) {
        this.requestContext = requestContext;
    }

    /**
     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {

//        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
//        boolean allTruePredicates = targetingPredicates
//                .stream()
//                .map(targetingPredicate -> targetingPredicate.evaluate(requestContext))
//                .allMatch(t-> t.isTrue())
//                ;

//        for (TargetingPredicate predicate : targetingPredicates) {
//            TargetingPredicateResult predicateResult = predicate.evaluate(requestContext);
//            if (!predicateResult.isTrue()) {
//                allTruePredicates = false;
//                break;
//            }
//        }
        return targetingGroup.getTargetingPredicates()
                .stream()
                .map(x -> x.evaluate(requestContext))
                .anyMatch(x -> !x.isTrue()) ?
                TargetingPredicateResult.FALSE : TargetingPredicateResult.TRUE;
    }
}
