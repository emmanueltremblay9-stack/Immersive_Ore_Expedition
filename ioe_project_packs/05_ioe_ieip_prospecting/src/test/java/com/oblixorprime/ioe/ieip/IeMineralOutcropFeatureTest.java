package com.oblixorprime.ioe.ieip;

import com.oblixorprime.ioe.core.ResourcePolicyService;
import com.oblixorprime.ioe.core.ResourceRef;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class IeMineralOutcropFeatureTest {
    private final IeMineralOutcropFeature feature = new IeMineralOutcropFeature();
    private final ResourcePolicyService policy = new ResourcePolicyService();

    @Test
    void createsPlanForLoadedIeBlockResourceOnly() {
        ResourceRef loadedLead = ResourceRef.block("immersiveengineering", "lead_ore");
        IeMineralDepositRef deposit = new IeMineralDepositRef("ie:lead", List.of(loadedLead));
        ProspectingTestScanner scanner = scanner(Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING), Set.of(loadedLead));

        Optional<MineralOutcropPlan> plan = feature.planOutcropClue(deposit, 99, scanner, policy);

        assertTrue(plan.isPresent());
        assertEquals(loadedLead, plan.get().clueResource());
        assertEquals(5, plan.get().boulderCount());
        assertEquals(3, plan.get().freeOreRewardLimitBlocks());
        assertTrue(plan.get().usesDepositPresentResourcesOnly());
        assertFalse(plan.get().rendersFullDeposit());
    }

    @Test
    void skipsOutcropPlanningWhenImmersiveEngineeringIsMissing() {
        ResourceRef loadedLead = ResourceRef.block("immersiveengineering", "lead_ore");
        IeMineralDepositRef deposit = new IeMineralDepositRef("ie:lead", List.of(loadedLead));

        Optional<MineralOutcropPlan> plan = feature.planOutcropClue(deposit, 3, scanner(Set.of(), Set.of(loadedLead)), policy);

        assertTrue(plan.isEmpty());
    }

    @Test
    void rejectedOrMissingResourcesDoNotCreateFallbackOutcrops() {
        ResourceRef rejectedTin = ResourceRef.block("immersiveengineering", "tin_ore");
        ResourceRef missingSilver = ResourceRef.block("immersiveengineering", "silver_ore");
        IeMineralDepositRef deposit = new IeMineralDepositRef("ie:unsafe", List.of(rejectedTin, missingSilver));
        ProspectingTestScanner scanner = scanner(Set.of(ProspectingCompatGates.IMMERSIVE_ENGINEERING), Set.of());

        Optional<MineralOutcropPlan> plan = feature.planOutcropClue(deposit, 3, scanner, policy);

        assertTrue(plan.isEmpty());
    }

    private static ProspectingTestScanner scanner(Set<String> mods, Set<ResourceRef> resources) {
        return new ProspectingTestScanner(mods, resources);
    }
}
