package com.yalantis.filter.animator

import android.animation.ValueAnimator
import android.support.v4.animation.AnimatorCompatHelper
import android.support.v4.view.ViewCompat
import android.support.v4.view.ViewPropertyAnimatorListener
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.View
import java.util.*

/**
 * Created by irinagalata on 9/22/16.
 */
class FiltersListItemAnimator : SimpleItemAnimator() {

    override fun isRunning(): Boolean {
        return !mPendingAdditions.isEmpty() ||
                !mPendingChanges.isEmpty() ||
                !mPendingMoves.isEmpty() ||
                !mPendingRemovals.isEmpty() ||
                !mMoveAnimations.isEmpty() ||
                !mRemoveAnimations.isEmpty() ||
                !mAddAnimations.isEmpty() ||
                !mChangeAnimations.isEmpty() ||
                !mMovesList.isEmpty() ||
                !mAdditionsList.isEmpty() ||
                !mChangesList.isEmpty()
    }

    private val mPendingRemovals = mutableListOf<RecyclerView.ViewHolder>()
    private val mPendingAdditions = mutableListOf<RecyclerView.ViewHolder>()
    private val mPendingMoves = mutableListOf<MoveInfo>()
    private val mPendingChanges = mutableListOf<ChangeInfo>()

    private val mAdditionsList = mutableListOf<ArrayList<RecyclerView.ViewHolder>>()
    private val mMovesList = mutableListOf<ArrayList<MoveInfo>>()
    private val mChangesList = mutableListOf<ArrayList<ChangeInfo>>()

    private val mAddAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val mMoveAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val mRemoveAnimations = mutableListOf<RecyclerView.ViewHolder>()
    private val mChangeAnimations = mutableListOf<RecyclerView.ViewHolder>()

    data class MoveInfo(var holder: RecyclerView.ViewHolder, var fromX: Int, var fromY: Int, var toX: Int, var toY: Int)

    data class ChangeInfo(var oldHolder: RecyclerView.ViewHolder?, var newHolder: RecyclerView.ViewHolder?,
                          var fromX: Int = 0,
                          var fromY: Int = 0,
                          var toX: Int = 0,
                          var toY: Int = 0)


    override fun runPendingAnimations() {
        val removalsPending = !mPendingRemovals.isEmpty()
        val movesPending = !mPendingMoves.isEmpty()
        val changesPending = !mPendingChanges.isEmpty()
        val additionsPending = !mPendingAdditions.isEmpty()
        if (!removalsPending && !movesPending && !additionsPending && !changesPending) {
            // nothing to animate
            return
        }
        // First, remove stuff
        for (holder in mPendingRemovals) {
            animateRemoveImpl(holder)
        }
        mPendingRemovals.clear()
        // Next, move stuff
        if (movesPending) {
            val moves = ArrayList<MoveInfo>()
            moves.addAll(mPendingMoves)
            mMovesList.add(moves)
            mPendingMoves.clear()
            val mover = Runnable {
                for ((holder, fromX, fromY, toX, toY) in moves) {
                    animateMoveImpl(holder, fromX, fromY,
                            toX, toY)
                }
                moves.clear()
                mMovesList.remove(moves)
            }
            if (removalsPending) {
                val view = moves[0].holder.itemView
                ViewCompat.postOnAnimationDelayed(view, mover, getRemoveDuration())
            } else {
                mover.run()
            }
        }
        // Next, change stuff, to run in parallel with move animations
        if (changesPending) {
            val changes = ArrayList<ChangeInfo>()
            changes.addAll(mPendingChanges)
            mChangesList.add(changes)
            mPendingChanges.clear()
            val changer = Runnable {
                for (change in changes) {
                    animateChangeImpl(change)
                }
                changes.clear()
                mChangesList.remove(changes)
            }
            if (removalsPending) {
                val holder = changes[0].oldHolder
                ViewCompat.postOnAnimationDelayed(holder!!.itemView, changer, getRemoveDuration())
            } else {
                changer.run()
            }
        }
        // Next, add stuff
        if (additionsPending) {
            val additions = ArrayList<RecyclerView.ViewHolder>()
            additions.addAll(mPendingAdditions)
            mAdditionsList.add(additions)
            mPendingAdditions.clear()
            val adder = Runnable {
                for (holder in additions) {
                    animateAddImpl(holder)
                }
                additions.clear()
                mAdditionsList.remove(additions)
            }
            if (removalsPending || movesPending || changesPending) {
                val removeDuration = (if (removalsPending) removeDuration else 0).toLong()
                val moveDuration = (if (movesPending) moveDuration else 0).toLong()
                val changeDuration = (if (changesPending) changeDuration else 0).toLong()
                val totalDelay = removeDuration + Math.max(moveDuration, changeDuration)
                val view = additions[0].itemView
                ViewCompat.postOnAnimationDelayed(view, adder, totalDelay)
            } else {
                adder.run()
            }
        }
    }

    override fun animateRemove(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        mPendingRemovals.add(holder)
        return true
    }

    private fun animateRemoveImpl(holder: RecyclerView.ViewHolder) {
        val view = holder.itemView

        ValueAnimator.ofFloat(0f, 300f).setDuration(300).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / 300

                view.scaleX = 1 - ratio
                view.scaleY = 1 - ratio
                view.alpha = 1 - ratio

                if (ratio == 1f) {
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.alpha = 1f
                    dispatchRemoveFinished(holder)
                    mRemoveAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }
        }.start()
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder): Boolean {
        resetAnimation(holder)
        ViewCompat.setAlpha(holder.itemView, 0f)
        mPendingAdditions.add(holder)
        return true
    }

    private fun animateAddImpl(holder: RecyclerView.ViewHolder) {
        val view = holder.itemView
        mAddAnimations.add(holder)

        ValueAnimator.ofFloat(0f, 300f).setDuration(300).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / 300

                view.scaleX = ratio
                view.scaleY = ratio
                view.alpha = ratio

                if (ratio == 1f) {
                    dispatchAddFinished(holder)
                    mAddAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }
        }.start()
    }

    override fun animateMove(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int,
                             toX: Int, toY: Int): Boolean {
        var fromX = fromX
        var fromY = fromY
        val view = holder.itemView
        fromX += ViewCompat.getTranslationX(holder.itemView).toInt()
        fromY += ViewCompat.getTranslationY(holder.itemView).toInt()
        resetAnimation(holder)
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX == 0 && deltaY == 0) {
            dispatchMoveFinished(holder)
            return false
        }
        if (deltaX != 0) {
            ViewCompat.setTranslationX(view, (-deltaX).toFloat())
        }
        if (deltaY != 0) {
            ViewCompat.setTranslationY(view, (-deltaY).toFloat())
        }
        mPendingMoves += MoveInfo(holder, fromX, fromY, toX, toY)
        return true
    }

    private fun animateMoveImpl(holder: RecyclerView.ViewHolder, fromX: Int, fromY: Int, toX: Int, toY: Int) {
        val view = holder.itemView
        val deltaX = toX - fromX
        val deltaY = toY - fromY
        if (deltaX != 0) {
            ViewCompat.animate(view).translationX(0f)
        }
        if (deltaY != 0) {
            ViewCompat.animate(view).translationY(0f)
        }
        mMoveAnimations.add(holder)

        ValueAnimator.ofFloat(0f, 300f).setDuration(300).apply {
            addUpdateListener {
                val ratio = it.animatedValue as Float / 300

                view.scaleX = ratio
                view.scaleY = ratio
                view.alpha = ratio

                if (ratio == 1f) {
                    dispatchMoveFinished(holder)
                    mMoveAnimations.remove(holder)
                    dispatchFinishedWhenDone()
                }
            }
        }.start()
    }

    override fun animateChange(oldHolder: RecyclerView.ViewHolder, newHolder: RecyclerView.ViewHolder?,
                               fromX: Int, fromY: Int, toX: Int, toY: Int): Boolean {
        if (oldHolder === newHolder) {
            // Don't know how to run change animations when the same view holder is re-used.
            // run a move animation to handle position changes.
            return animateMove(oldHolder, fromX, fromY, toX, toY)
        }
        val prevTranslationX = ViewCompat.getTranslationX(oldHolder.itemView)
        val prevTranslationY = ViewCompat.getTranslationY(oldHolder.itemView)
        val prevAlpha = ViewCompat.getAlpha(oldHolder.itemView)
        resetAnimation(oldHolder)
        val deltaX = (toX.toFloat() - fromX.toFloat() - prevTranslationX).toInt()
        val deltaY = (toY.toFloat() - fromY.toFloat() - prevTranslationY).toInt()
        // recover prev translation state after ending animation
        ViewCompat.setTranslationX(oldHolder.itemView, prevTranslationX)
        ViewCompat.setTranslationY(oldHolder.itemView, prevTranslationY)
        ViewCompat.setAlpha(oldHolder.itemView, prevAlpha)
        if (newHolder != null) {
            // carry over translation values
            resetAnimation(newHolder)
            ViewCompat.setTranslationX(newHolder.itemView, (-deltaX).toFloat())
            ViewCompat.setTranslationY(newHolder.itemView, (-deltaY).toFloat())
            ViewCompat.setAlpha(newHolder.itemView, 0f)
        }
        mPendingChanges += ChangeInfo(oldHolder, newHolder, fromX, fromY, toX, toY)
        return true
    }

    private fun animateChangeImpl(changeInfo: ChangeInfo) {
        val holder = changeInfo.oldHolder
        val view = holder?.itemView
        val newHolder = changeInfo.newHolder
        val newView = newHolder?.itemView
        if (view != null) {
            mChangeAnimations.add(changeInfo.oldHolder!!)

            view.translationX = (changeInfo.toX - changeInfo.fromX).toFloat()
            view.translationY = (changeInfo.toY - changeInfo.fromY).toFloat()

            ValueAnimator.ofFloat(0f, 300f).setDuration(300).apply {
                addUpdateListener {
                    val ratio = it.animatedValue as Float / 300

                    view.scaleX = 1 - ratio
                    view.scaleY = 1 - ratio
                    view.alpha = 1 - ratio

                    if (ratio == 1f) {
                        view.alpha = 1f
                        view.translationX = 0f
                        view.translationY = 0f
                        dispatchChangeFinished(changeInfo.oldHolder, true)
                        mChangeAnimations.remove(changeInfo.oldHolder!!)
                        dispatchFinishedWhenDone()
                    }
                }
            }.start()
        }
        if (newView != null) {
            mChangeAnimations.add(changeInfo.newHolder!!)

            ValueAnimator.ofFloat(0f, 300f).setDuration(300).apply {
                addUpdateListener {
                    val ratio = it.animatedValue as Float / 300

                    newView.scaleX = ratio
                    newView.scaleY = ratio
                    newView.alpha = ratio
                    newView.translationX = 1 - ratio
                    newView.translationY = 1 - ratio

                    if (ratio == 1f) {
                        newView.alpha = 1f
                        newView.translationX = 0f
                        newView.translationY = 0f
                        dispatchChangeFinished(changeInfo.newHolder, false)
                        mChangeAnimations.remove(changeInfo.newHolder!!)
                        dispatchFinishedWhenDone()
                    }
                }
            }.start()
        }
    }

    private fun endChangeAnimation(infoList: MutableList<ChangeInfo>, item: RecyclerView.ViewHolder) {
        for (i in infoList.indices.reversed()) {
            val changeInfo = infoList[i]
            if (endChangeAnimationIfNecessary(changeInfo, item)) {
                if (changeInfo.oldHolder == null && changeInfo.newHolder == null) {
                    infoList.remove(changeInfo)
                }
            }
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo) {
        if (changeInfo.oldHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.oldHolder!!)
        }
        if (changeInfo.newHolder != null) {
            endChangeAnimationIfNecessary(changeInfo, changeInfo.newHolder!!)
        }
    }

    private fun endChangeAnimationIfNecessary(changeInfo: ChangeInfo, item: RecyclerView.ViewHolder): Boolean {
        var oldItem = false
        if (changeInfo.newHolder === item) {
            changeInfo.newHolder = null
        } else if (changeInfo.oldHolder === item) {
            changeInfo.oldHolder = null
            oldItem = true
        } else {
            return false
        }
        ViewCompat.setAlpha(item.itemView, 1f)
        ViewCompat.setTranslationX(item.itemView, 0f)
        ViewCompat.setTranslationY(item.itemView, 0f)
        dispatchChangeFinished(item, oldItem)
        return true
    }

    override fun endAnimation(item: RecyclerView.ViewHolder) {
        val view = item.itemView
        // this will trigger end callback which should set properties to their target values.
        ViewCompat.animate(view).cancel()
        // TODO if some other animations are chained to end, how do we cancel them as well?
        for (i in mPendingMoves.indices.reversed()) {
            val moveInfo = mPendingMoves[i]
            if (moveInfo.holder === item) {
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(item)
                mPendingMoves.removeAt(i)
            }
        }
        endChangeAnimation(mPendingChanges, item)
        if (mPendingRemovals.remove(item)) {
            ViewCompat.setAlpha(view, 1f)
            dispatchRemoveFinished(item)
        }
        if (mPendingAdditions.remove(item)) {
            ViewCompat.setAlpha(view, 1f)
            dispatchAddFinished(item)
        }

        for (i in mChangesList.indices.reversed()) {
            val changes = mChangesList[i]
            endChangeAnimation(changes, item)
            if (changes.isEmpty()) {
                mChangesList.removeAt(i)
            }
        }
        for (i in mMovesList.indices.reversed()) {
            val moves = mMovesList[i]
            for (j in moves.indices.reversed()) {
                val moveInfo = moves[j]
                if (moveInfo.holder === item) {
                    ViewCompat.setTranslationY(view, 0f)
                    ViewCompat.setTranslationX(view, 0f)
                    dispatchMoveFinished(item)
                    moves.removeAt(j)
                    if (moves.isEmpty()) {
                        mMovesList.removeAt(i)
                    }
                    break
                }
            }
        }
        for (i in mAdditionsList.indices.reversed()) {
            val additions = mAdditionsList[i]
            if (additions.remove(item)) {
                ViewCompat.setAlpha(view, 1f)
                dispatchAddFinished(item)
                if (additions.isEmpty()) {
                    mAdditionsList.removeAt(i)
                }
            }
        }
        dispatchFinishedWhenDone()
    }

    private fun resetAnimation(holder: RecyclerView.ViewHolder) {
        AnimatorCompatHelper.clearInterpolator(holder.itemView)
        endAnimation(holder)
    }


    /**
     * Check the state of currently pending and running animations. If there are none
     * pending/running, call [.dispatchAnimationsFinished] to notify any
     * listeners.
     */
    private fun dispatchFinishedWhenDone() {
        if (!isRunning) {
            dispatchAnimationsFinished()
        }
    }

    override fun endAnimations() {
        var count = mPendingMoves.size
        for (i in count - 1 downTo 0) {
            val item = mPendingMoves[i]
            val view = item.holder.itemView
            ViewCompat.setTranslationY(view, 0f)
            ViewCompat.setTranslationX(view, 0f)
            dispatchMoveFinished(item.holder)
            mPendingMoves.removeAt(i)
        }
        count = mPendingRemovals.size
        for (i in count - 1 downTo 0) {
            val item = mPendingRemovals[i]
            dispatchRemoveFinished(item)
            mPendingRemovals.removeAt(i)
        }
        count = mPendingAdditions.size
        for (i in count - 1 downTo 0) {
            val item = mPendingAdditions[i]
            val view = item.itemView
            ViewCompat.setAlpha(view, 1f)
            dispatchAddFinished(item)
            mPendingAdditions.removeAt(i)
        }
        count = mPendingChanges.size
        for (i in count - 1 downTo 0) {
            endChangeAnimationIfNecessary(mPendingChanges[i])
        }
        mPendingChanges.clear()
        if (!isRunning) {
            return
        }

        var listCount = mMovesList.size
        for (i in listCount - 1 downTo 0) {
            val moves = mMovesList[i]
            count = moves.size
            for (j in count - 1 downTo 0) {
                val moveInfo = moves[j]
                val item = moveInfo.holder
                val view = item.itemView
                ViewCompat.setTranslationY(view, 0f)
                ViewCompat.setTranslationX(view, 0f)
                dispatchMoveFinished(moveInfo.holder)
                moves.removeAt(j)
                if (moves.isEmpty()) {
                    mMovesList.remove(moves)
                }
            }
        }
        listCount = mAdditionsList.size
        for (i in listCount - 1 downTo 0) {
            val additions = mAdditionsList[i]
            count = additions.size
            for (j in count - 1 downTo 0) {
                val item = additions[j]
                val view = item.itemView
                ViewCompat.setAlpha(view, 1f)
                dispatchAddFinished(item)
                additions.removeAt(j)
                if (additions.isEmpty()) {
                    mAdditionsList.remove(additions)
                }
            }
        }
        listCount = mChangesList.size
        for (i in listCount - 1 downTo 0) {
            val changes = mChangesList[i]
            count = changes.size
            for (j in count - 1 downTo 0) {
                endChangeAnimationIfNecessary(changes[j])
                if (changes.isEmpty()) {
                    mChangesList.remove(changes)
                }
            }
        }

        cancelAll(mRemoveAnimations)
        cancelAll(mMoveAnimations)
        cancelAll(mAddAnimations)
        cancelAll(mChangeAnimations)

        dispatchAnimationsFinished()
    }

    internal fun cancelAll(viewHolders: List<RecyclerView.ViewHolder>) {
        for (i in viewHolders.indices.reversed()) {
            ViewCompat.animate(viewHolders[i].itemView).cancel()
        }
    }

    /**
     * {@inheritDoc}
     *
     *
     * If the payload list is not empty, DefaultItemAnimator returns `true`.
     * When this is the case:
     *
     *  * If you override [.animateChange], both
     * ViewHolder arguments will be the same instance.
     *
     *  *
     * If you are not overriding [.animateChange],
     * then DefaultItemAnimator will call [.animateMove] and
     * run a move animation instead.
     *
     *
     */
    override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder,
                                           payloads: List<Any>): Boolean {
        return !payloads.isEmpty() || super.canReuseUpdatedViewHolder(viewHolder, payloads)
    }

    private open class VpaListenerAdapter : ViewPropertyAnimatorListener {
        override fun onAnimationStart(view: View) {
        }

        override fun onAnimationEnd(view: View) {
        }

        override fun onAnimationCancel(view: View) {
        }
    }
}