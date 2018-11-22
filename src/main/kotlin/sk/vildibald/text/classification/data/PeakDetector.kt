package sk.vildibald.text.classification.data

import sk.vildibald.text.classification.data.entities.BtcPrice

const val DEFAULT_TRIGGER_DELTA = 350.0

data class Peak(val height: Double, val index: Int)

data class Valley(val height: Double, val index: Int)

data class PeaksValleys(val peaks: List<Peak>, val valleys: List<Valley>)

class PeakDetector {

    /*
     !PEAKDET Detect peaks in a vector
     !
     !        call PEAKDET(MAXTAB, MINTAB, N, V, DELTA) finds the local
     !        maxima and minima ("peaks") in the vector V of size N.
     !        MAXTAB and MINTAB consists of two columns. Column 1
     !        contains indices in V, and column 2 the found values.
     !
     !        call PEAKDET(MAXTAB, MINTAB, N, V, DELTA, X) replaces the
     !        indices in MAXTAB and MINTAB with the corresponding X-values.
     !
     !        A point is considered a maximum peak if it has the maximal
     !        value, and was preceded (to the left) by a value lower by
     !        DELTA.
     !
     ! Eli Billauer, 3.4.05 (http://billauer.co.il)
     ! Translated into Fortran by Brian McNoldy (http://andrew.rsmas.miami.edu/bmcnoldy)
     ! This function is released to the public domain; Any use is allowed.*/
    fun detectPeaks(vector: List<BtcPrice>,
                    offset: Int = 0,
                    length: Int = vector.size,
                    triggerDelta: Double = DEFAULT_TRIGGER_DELTA):
            PeaksValleys {
        var mn = Double.POSITIVE_INFINITY
        var mx = Double.NEGATIVE_INFINITY
        var mnpos = Double.NaN
        var mxpos = Double.NaN
        var lookformax = true

        val maxtab_tmp = ArrayList<Peak>()
        val mintab_tmp = ArrayList<Valley>();

        for (i in offset until length) {
            val a = vector[i].value
            if (a > mx) {
                mx = a
                mxpos = vector[i].value
            }
            if (a < mn) {
                mn = a
                mnpos = vector[i].value
            }
            if (lookformax) {
                if (a < mx - triggerDelta) {
                    maxtab_tmp.add(Peak(mxpos, i))
                    mn = a
                    mnpos = vector[i].value
                    lookformax = false
                }
            } else {
                if (a > mn + triggerDelta) {
                    mintab_tmp.add(Valley(mnpos, i));
                    mx = a
                    mxpos = vector[i].value
                    lookformax = true
                }
            }
        }

        return PeaksValleys(maxtab_tmp, mintab_tmp)
    }
}

fun List<BtcPrice>.detectPeaks(): PeaksValleys =
        PeakDetector().detectPeaks(this)