package com.example.sebastian.vocbox;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Random;

public class VocCaseModel implements Serializable {
    private static final long serialVersionUID = 7526947251620173593L;

    static class VocCardModel implements Serializable {
        private static final long serialVersionUID = 8361540116453755816L;

        private String mNative;
        private String mForeign;

        VocCardModel(String nativeWord, String foreignWord) {
            mNative = nativeWord;
            mForeign = foreignWord;
        }

        String getNative() {
            return mNative;
        }

        String getForeign() {
            return mForeign;
        }
    }

    private static final Random random = new Random();

    private final LinkedList<VocCardModel> mBackendCards = new LinkedList<>();
    private final LinkedList<VocCardModel> mCurrentCards = new LinkedList<>();
    private final int mMaxCurrentCardCount;
    private boolean mIsAnswerOpen = false;

    VocCaseModel(int maxCurrentCardCount) {
        if (maxCurrentCardCount < 1)
            throw new RuntimeException("Invalid max current card count.");
        mMaxCurrentCardCount = maxCurrentCardCount;
    }

    int getCardCount() {
        return mBackendCards.size() + mCurrentCards.size();
    }

    VocCardModel getCurrentCard() {
        return mCurrentCards.getFirst();
    }

    VocCardModel removeCurrentCardRandomReplacement() {
        if (!mBackendCards.isEmpty())
            mCurrentCards.addLast(mBackendCards.remove(random.nextInt(mBackendCards.size())));
        return mCurrentCards.removeFirst();
    }

    VocCardModel removeCurrentCardNonRandomReplacement() {
        if (!mBackendCards.isEmpty())
            mCurrentCards.addLast((mBackendCards.removeFirst()));
        return mCurrentCards.removeFirst();
    }

    void recycleCurrentCard() {
        mCurrentCards.addLast(mCurrentCards.removeFirst());
    }

    void addCardAtRandomPosition(VocCardModel card) {
        int position = random.nextInt(getCardCount() + 1);
        if (position < mMaxCurrentCardCount) {
            mCurrentCards.add(position, card);
            if (mCurrentCards.size() > mMaxCurrentCardCount)
                mBackendCards.addFirst(mCurrentCards.removeLast());
        } else {
            mBackendCards.add(position - mMaxCurrentCardCount, card);
        }
    }

    void addCardAtBack(VocCardModel card) {
        if (mCurrentCards.size() < mMaxCurrentCardCount)
            mCurrentCards.addLast(card);
        else
            mBackendCards.addLast(card);
    }

    boolean isAnswerOpen() {
        return mIsAnswerOpen;
    }

    void setIsAnswerOpen(boolean isAnswerOpen) {
        mIsAnswerOpen = isAnswerOpen;
    }
}
