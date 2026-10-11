package com.gianlucanatoli.padelscore.core

import kotlinx.serialization.Serializable

@Serializable
data class SetResult(val team1: Int, val team2: Int)

@Serializable
data class MatchState(
    val startedAtEpochMs: Long,
    val finishedAtEpochMs: Long? = null,
    val team1Sets: Int = 0,
    val team2Sets: Int = 0,
    val team1Games: Int = 0,
    val team2Games: Int = 0,
    val team1Points: Int = 0,
    val team2Points: Int = 0,
    val team1Advantage: Boolean = false,
    val team2Advantage: Boolean = false,
    val inTieBreak: Boolean = false,
    val tieBreakTeam1: Int = 0,
    val tieBreakTeam2: Int = 0,
    val completedSets: List<SetResult> = emptyList()
) {
    val isFinished: Boolean get() = finishedAtEpochMs != null
}

object PadelScoring {
    fun newMatch(now: Long = System.currentTimeMillis()) = MatchState(startedAtEpochMs = now)

    fun point(state: MatchState, team: Int): MatchState {
        require(team == 1 || team == 2)
        if (state.isFinished) return state

        if (state.inTieBreak) return tieBreakPoint(state, team)

        val p1 = state.team1Points
        val p2 = state.team2Points

        // 40-40: advantage rules.
        if (p1 == 40 && p2 == 40) {
            return when {
                team == 1 && !state.team1Advantage && !state.team2Advantage -> state.copy(team1Advantage = true)
                team == 2 && !state.team1Advantage && !state.team2Advantage -> state.copy(team2Advantage = true)
                team == 1 && state.team2Advantage -> state.copy(team2Advantage = false)
                team == 2 && state.team1Advantage -> state.copy(team1Advantage = false)
                team == 1 && state.team1Advantage -> winGame(state, 1)
                else -> winGame(state, 2)
            }
        }

        if (team == 1 && state.team1Advantage) return winGame(state, 1)
        if (team == 2 && state.team2Advantage) return winGame(state, 2)

        if (team == 1 && p1 == 40) return if (p2 < 40) winGame(state, 1) else state.copy(team1Advantage = true)
        if (team == 2 && p2 == 40) return if (p1 < 40) winGame(state, 2) else state.copy(team2Advantage = true)

        val next = if (team == 1) nextPoint(p1) else nextPoint(p2)
        return if (team == 1) state.copy(team1Points = next) else state.copy(team2Points = next)
    }

    private fun nextPoint(p: Int) = when (p) { 0 -> 15; 15 -> 30; 30 -> 40; else -> 40 }

    private fun winGame(state: MatchState, team: Int): MatchState {
        val g1 = state.team1Games + if (team == 1) 1 else 0
        val g2 = state.team2Games + if (team == 2) 1 else 0

        // 6-6 starts a tie-break rather than creating a normal seventh game.
        if (g1 == 6 && g2 == 6) {
            return state.copy(team1Games = g1, team2Games = g2, team1Points = 0, team2Points = 0,
                team1Advantage = false, team2Advantage = false, inTieBreak = true, tieBreakTeam1 = 0, tieBreakTeam2 = 0)
        }

        val setWon = (g1 >= 6 && g1 - g2 >= 2) || (g2 >= 6 && g2 - g1 >= 2)
        if (setWon) {
            val set1 = g1 > g2
            val s1 = state.team1Sets + if (set1) 1 else 0
            val s2 = state.team2Sets + if (!set1) 1 else 0
            val sets = state.completedSets + SetResult(g1, g2)
            if (s1 == 2 || s2 == 2) {
                return state.copy(team1Sets = s1, team2Sets = s2, team1Games = g1, team2Games = g2,
                    team1Points = 0, team2Points = 0, team1Advantage = false, team2Advantage = false,
                    completedSets = sets, finishedAtEpochMs = System.currentTimeMillis())
            }
            return state.copy(team1Sets = s1, team2Sets = s2, team1Games = 0, team2Games = 0,
                team1Points = 0, team2Points = 0, team1Advantage = false, team2Advantage = false,
                completedSets = sets)
        }

        return state.copy(team1Games = g1, team2Games = g2, team1Points = 0, team2Points = 0,
            team1Advantage = false, team2Advantage = false)
    }

    private fun tieBreakPoint(state: MatchState, team: Int): MatchState {
        val t1 = state.tieBreakTeam1 + if (team == 1) 1 else 0
        val t2 = state.tieBreakTeam2 + if (team == 2) 1 else 0
        val won = (t1 >= 7 && t1 - t2 >= 2) || (t2 >= 7 && t2 - t1 >= 2)
        if (!won) return state.copy(tieBreakTeam1 = t1, tieBreakTeam2 = t2)

        val s1 = state.team1Sets + if (t1 > t2) 1 else 0
        val s2 = state.team2Sets + if (t2 > t1) 1 else 0
        val sets = state.completedSets + SetResult(7, 6)
        if (s1 == 2 || s2 == 2) {
            return state.copy(team1Sets = s1, team2Sets = s2, team1Games = 6, team2Games = 6,
                inTieBreak = false, tieBreakTeam1 = 0, tieBreakTeam2 = 0, completedSets = sets,
                finishedAtEpochMs = System.currentTimeMillis())
        }
        return state.copy(team1Sets = s1, team2Sets = s2, team1Games = 0, team2Games = 0,
            team1Points = 0, team2Points = 0, team1Advantage = false, team2Advantage = false,
            inTieBreak = false, tieBreakTeam1 = 0, tieBreakTeam2 = 0, completedSets = sets)
    }

    fun displayPoint(state: MatchState, team: Int): String {
        if (state.inTieBreak) return if (team == 1) state.tieBreakTeam1.toString() else state.tieBreakTeam2.toString()
        if (team == 1 && state.team1Advantage) return "AD"
        if (team == 2 && state.team2Advantage) return "AD"
        val p = if (team == 1) state.team1Points else state.team2Points
        return p.toString()
    }

    fun finish(state: MatchState, now: Long = System.currentTimeMillis()) = if (state.isFinished) state else state.copy(finishedAtEpochMs = now)
}
