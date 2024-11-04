package lotto.service;

import camp.nextstep.edu.missionutils.Randoms;
import lotto.model.Lotto;
import lotto.model.LottoNumbers;
import lotto.model.LottoRank;
import lotto.model.WinningLotto;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LottoService {
    private static final int PRICE_PER_LOTTO = 1000;
    private static final int LOTTO_SIZE = 6;

    public void validatePurchaseAmount(String input) {
        try {
            int amount = Integer.parseInt(input);
            validateAmountRange(amount);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("[ERROR] 구입 금액은 숫자여야 합니다.");
        }
    }

    private void validateAmountRange(int amount) {
        if (amount < PRICE_PER_LOTTO) {
            throw new IllegalArgumentException("[ERROR] 구입 금액은 1000원 이상이어야 합니다.");
        }
        if (amount % PRICE_PER_LOTTO != 0) {
            throw new IllegalArgumentException("[ERROR] 구입 금액은 1000원 단위여야 합니다.");
        }
    }

    public int calculateLottoCount(int purchaseAmount) {
        return purchaseAmount / PRICE_PER_LOTTO;
    }

    public List<Lotto> generateLottos(int count) {
        List<Lotto> lottos = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lottos.add(generateLotto());
        }
        return lottos;
    }

    private Lotto generateLotto() {
        List<Integer> numbers = Randoms.pickUniqueNumbersInRange(1, 45, LOTTO_SIZE);
        LottoNumbers.validateNumbers(numbers);
        return new Lotto(numbers);
    }

    public WinningLotto createWinningLotto(List<Integer> winningNumbers, int bonusNumber) {
        LottoNumbers.validateNumbers(winningNumbers);
        Lotto winningLotto = new Lotto(winningNumbers);
        return new WinningLotto(winningLotto, bonusNumber);
    }

    public Map<LottoRank, Integer> calculateWinningStatistics(List<Lotto> lottos, WinningLotto winningLotto) {
        Map<LottoRank, Integer> statistics = new EnumMap<>(LottoRank.class);
        initializeStatistics(statistics);

        for (Lotto lotto : lottos) {
            LottoRank rank = determineWinningRank(lotto, winningLotto);
            statistics.put(rank, statistics.get(rank) + 1);
        }

        return statistics;
    }

    private void initializeStatistics(Map<LottoRank, Integer> statistics) {
        for (LottoRank rank : LottoRank.values()) {
            statistics.put(rank, 0);
        }
    }

    private LottoRank determineWinningRank(Lotto lotto, WinningLotto winningLotto) {
        int matchCount = winningLotto.matchNumbersCount(lotto);
        boolean matchBonus = winningLotto.matchBonusNumber(lotto);
        return LottoRank.valueOf(matchCount, matchBonus);
    }

    public double calculateReturnRate(Map<LottoRank, Integer> statistics, int purchaseAmount) {
        long totalPrize = calculateTotalPrize(statistics);
        return (totalPrize * 100.0) / purchaseAmount;
    }

    private long calculateTotalPrize(Map<LottoRank, Integer> statistics) {
        return statistics.entrySet().stream()
                .mapToLong(entry -> (long) entry.getKey().getPrize() * entry.getValue())
                .sum();
    }
}