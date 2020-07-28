package com.kirkbushman.araw.clients

import androidx.annotation.IntRange
import androidx.annotation.WorkerThread
import com.kirkbushman.araw.RedditApi
import com.kirkbushman.araw.fetcher.CommentsFetcher
import com.kirkbushman.araw.fetcher.Fetcher
import com.kirkbushman.araw.fetcher.SubmissionsFetcher
import com.kirkbushman.araw.fetcher.SubmissionsSearchFetcher
import com.kirkbushman.araw.models.Comment
import com.kirkbushman.araw.models.MoreComments
import com.kirkbushman.araw.models.Submission
import com.kirkbushman.araw.models.TrendingSubreddits
import com.kirkbushman.araw.models.general.CommentsSorting
import com.kirkbushman.araw.models.general.SearchSorting
import com.kirkbushman.araw.models.general.SubmissionsSorting
import com.kirkbushman.araw.models.general.TimePeriod
import com.kirkbushman.araw.models.general.Vote
import com.kirkbushman.araw.models.mixins.CommentData
import com.kirkbushman.araw.models.mixins.Replyable
import com.kirkbushman.araw.models.mixins.Saveable
import com.kirkbushman.araw.models.mixins.SubredditData
import com.kirkbushman.araw.models.mixins.Votable
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class ContributionsClient(

    private val api: RedditApi,
    private inline val getHeaderMap: () -> HashMap<String, String>

) : BaseRedditClient(api, getHeaderMap) {

    @WorkerThread
    fun submission(submissionId: String, disableLegacyEncoding: Boolean = false): Submission? {

        val authMap = getHeaderMap()
        val req = api.submission(
            submissionId = "t3_$submissionId",
            rawJson = (if (disableLegacyEncoding) 1 else null),
            header = authMap
        )

        val res = req.execute()
        if (!res.isSuccessful) {
            return null
        }

        // take the first in the listing
        return res.body()?.data?.children?.firstOrNull()?.data
    }

    fun submissions(

        subreddit: SubredditData,

        @IntRange(from = Fetcher.MIN_LIMIT, to = Fetcher.MAX_LIMIT)
        limit: Long = Fetcher.DEFAULT_LIMIT,

        sorting: SubmissionsSorting = SubmissionsFetcher.DEFAULT_SORTING,
        timePeriod: TimePeriod = SubmissionsFetcher.DEFAULT_TIMEPERIOD,

        disableLegacyEncoding: Boolean = false
    ): SubmissionsFetcher {

        return submissions(
            subreddit = subreddit.displayName,
            limit = limit,
            sorting = sorting,
            timePeriod = timePeriod,
            disableLegacyEncoding = disableLegacyEncoding
        )
    }

    fun submissions(

        subreddit: String,

        @IntRange(from = Fetcher.MIN_LIMIT, to = Fetcher.MAX_LIMIT)
        limit: Long = Fetcher.DEFAULT_LIMIT,

        sorting: SubmissionsSorting = SubmissionsFetcher.DEFAULT_SORTING,
        timePeriod: TimePeriod = SubmissionsFetcher.DEFAULT_TIMEPERIOD,

        disableLegacyEncoding: Boolean = false

    ): SubmissionsFetcher {

        return SubmissionsFetcher(
            api = api,
            subreddit = subreddit,
            limit = limit,
            sorting = sorting,
            timePeriod = timePeriod,
            disableLegacyEncoding = disableLegacyEncoding,
            getHeader = getHeaderMap
        )
    }

    fun multiredditSubmissions(

        vararg subreddits: String,

        @IntRange(from = Fetcher.MIN_LIMIT, to = Fetcher.MAX_LIMIT)
        limit: Long = Fetcher.DEFAULT_LIMIT,

        sorting: SubmissionsSorting = SubmissionsFetcher.DEFAULT_SORTING,
        timePeriod: TimePeriod = SubmissionsFetcher.DEFAULT_TIMEPERIOD,

        disableLegacyEncoding: Boolean = false

    ): SubmissionsFetcher {

        return SubmissionsFetcher(
            api = api,
            subreddit = subreddits.joinToString(separator = "+"),
            limit = limit,
            sorting = sorting,
            timePeriod = timePeriod,
            disableLegacyEncoding = disableLegacyEncoding,
            getHeader = getHeaderMap
        )
    }

    fun submissionsSearch(

        subreddit: String?,
        query: String,

        @IntRange(from = Fetcher.MIN_LIMIT, to = Fetcher.MAX_LIMIT)
        limit: Long = Fetcher.DEFAULT_LIMIT,

        sorting: SearchSorting = SubmissionsSearchFetcher.DEFAULT_SORTING,
        timePeriod: TimePeriod = SubmissionsSearchFetcher.DEFAULT_TIMEPERIOD,

        restrictToSubreddit: Boolean = false,
        disableLegacyEncoding: Boolean = false

    ): SubmissionsSearchFetcher {

        return SubmissionsSearchFetcher(
            api = api,
            subreddit = subreddit,
            query = query,
            limit = limit,
            sorting = sorting,
            timePeriod = timePeriod,
            restrictToSubreddit = restrictToSubreddit,
            disableLegacyEncoding = disableLegacyEncoding,
            getHeader = getHeaderMap
        )
    }

    @WorkerThread
    fun comment(commentId: String, disableLegacyEncoding: Boolean = false): Comment? {

        val authMap = getHeaderMap()
        val req = api.comment(
            commentId = "t1_$commentId",
            rawJson = (if (disableLegacyEncoding) 1 else null),
            header = authMap
        )

        val res = req.execute()
        if (!res.isSuccessful) {
            return null
        }

        return res.body()?.data?.children?.firstOrNull()?.data
    }

    fun comments(

        submissionId: String,

        sorting: CommentsSorting = CommentsFetcher.DEFAULT_SORTING,

        @IntRange(from = Fetcher.MIN_LIMIT, to = Fetcher.MAX_LIMIT)
        limit: Long = Fetcher.DEFAULT_LIMIT,

        depth: Int? = null,
        disableLegacyEncoding: Boolean = false

    ): CommentsFetcher {

        return CommentsFetcher(
            api = api,
            submissionId = submissionId,
            sorting = sorting,
            limit = limit,
            depth = depth,
            disableLegacyEncoding = disableLegacyEncoding,
            getHeader = getHeaderMap
        )
    }

    @WorkerThread
    fun moreChildren(

        moreComments: MoreComments,
        submission: Submission,

        sorting: CommentsSorting? = null,
        limitChildren: Boolean? = null,
        depth: Int? = null,

        disableLegacyEncoding: Boolean = false

    ): List<CommentData>? {

        val authMap = getHeaderMap()
        val req = api.moreChildren(
            children = moreComments.children.joinToString(separator = ","),
            sorting = sorting?.sortingStr,
            limitChildren = limitChildren,
            depth = depth,
            id = moreComments.id,
            linkId = submission.fullname,
            rawJson = (if (disableLegacyEncoding) 1 else null),
            header = authMap
        )

        val res = req.execute()
        if (!res.isSuccessful) {
            return null
        }

        return res.body()?.json?.data?.things?.map { it.data }?.toList()
    }

    @WorkerThread
    fun trendingSubreddits(): TrendingSubreddits? {

        val req = api.trendingSubreddits()
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun delete(comment: Comment): Any? {
        return delete(comment.fullname)
    }

    @WorkerThread
    fun delete(submission: Submission): Any? {
        return delete(submission.fullname)
    }

    @WorkerThread
    fun delete(fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = api.delete(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun hide(submission: Submission): Any? {
        return hide(!submission.isHidden, submission.fullname)
    }

    @WorkerThread
    fun hide(hide: Boolean, submission: Submission): Any? {
        return hide(hide, submission)
    }

    @WorkerThread
    fun hide(hide: Boolean, fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = if (hide)
            api.hide(fullname, header = authMap)
        else
            api.unhide(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun lock(comment: Comment): Any? {
        return lock(!comment.isLocked, comment.fullname)
    }

    @WorkerThread
    fun lock(lock: Boolean, comment: Comment): Any? {
        return lock(lock, comment.fullname)
    }

    @WorkerThread
    fun lock(submission: Submission): Any? {
        return lock(!submission.isLocked, submission.fullname)
    }

    @WorkerThread
    fun lock(lock: Boolean, submission: Submission): Any? {
        return lock(lock, submission.fullname)
    }

    @WorkerThread
    fun lock(lock: Boolean, fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = if (lock)
            api.lock(fullname, header = authMap)
        else
            api.unlock(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun reply(replyable: Replyable, text: String): Comment? {
        return reply(replyable.fullname, text)
    }

    @WorkerThread
    fun reply(fullname: String, text: String): Comment? {

        val authMap = getHeaderMap()
        val req = api.reply(
            fullname = fullname,
            text = text,
            header = authMap
        )

        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()?.json?.data?.things?.firstOrNull()?.data
    }

    @WorkerThread
    fun save(save: Boolean, saveable: Saveable): Any? {
        return save(save, saveable.fullname)
    }

    @WorkerThread
    fun save(save: Boolean, fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = if (save)
            api.save(id = fullname, header = authMap)
        else
            api.unsave(id = fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun vote(vote: Vote, votable: Votable): Any? {
        return vote(vote, votable.fullname)
    }

    @WorkerThread
    fun vote(vote: Vote, fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = api.vote(id = fullname, dir = vote.dir, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun markAsNsfw(comment: Comment): Any? {
        return markAsNsfw(comment.fullname)
    }

    @WorkerThread
    fun markAsNsfw(submission: Submission): Any? {
        return markAsNsfw(submission.fullname)
    }

    @WorkerThread
    fun markAsNsfw(fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = api.markAsNsfw(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun unmarkAsNsfw(submission: Submission): Any? {
        return unmarkAsNsfw(submission.fullname)
    }

    @WorkerThread
    fun unmarkAsNsfw(fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = api.unmarknsfw(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun markAsSpoiler(comment: Comment): Any? {
        return markAsSpoiler(comment.fullname)
    }

    @WorkerThread
    fun markAsSpoiler(submission: Submission): Any? {
        return markAsSpoiler(submission.fullname)
    }

    @WorkerThread
    fun markAsSpoiler(fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = api.markAsSpoiler(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    @WorkerThread
    fun unspoiler(submission: Submission): Any? {
        return unspoiler(submission.fullname)
    }

    @WorkerThread
    fun unspoiler(fullname: String): Any? {

        val authMap = getHeaderMap()
        val req = api.unspoiler(fullname, header = authMap)
        val res = req.execute()

        if (!res.isSuccessful) {
            return null
        }

        return res.body()
    }

    // todo: consider making a separate module with an android GraphQL client,
    // todo: to avoid this ugliness
    /*@WorkerThread
    fun pollVote(submissionFullname: String, optionId: String): List<PollVoteStateOption>? {

        val authMap = getHeaderMap()
        val req = api.pollVote(
            // send to reddit's GraphQL url
            url = "https://gql.reddit.com",
            requestTimestamp = System.currentTimeMillis() / 1000L,
            pollVoteReq = PollVoteReq(
                // fixed id value
                id = "a20cc8dd230d",
                variables = PollVoteVariables(
                    input = PollVoteInput(
                        postId = submissionFullname,
                        optionId = optionId
                    )
                )
            ),
            header = authMap
        )

        val res = req.execute()
        if (!res.isSuccessful) {
            return null
        }

        return res.body()?.data?.updatePostPollVoteState?.poll?.options
    }*/

    @WorkerThread
    fun uploadMedia(filename: String, mimeType: String? = null, fileContent: ByteArray): String? {

        var currentMimeType = mimeType
        if (currentMimeType == null) {

            val extension = filename.substring(filename.lastIndexOf('.'))
            currentMimeType = when (extension) {
                "png" -> "image/png"
                "jpg" -> "image/jpeg"
                "jpeg" -> "image/jpeg"
                "gif" -> "image/gif"
                "webp" -> "image/webp"
                "mp4" -> "video/mp4"
                "mpeg" -> "video/mpeg"
                "webm" -> "video/webm"

                else -> throw IllegalStateException("Media's mimetype not supported! check the file extension.")
            }
        }

        val authMap = getHeaderMap()
        val req = api.obtainUploadContract(
            filepath = filename,
            mimetype = currentMimeType,
            header = authMap
        )

        val uploadContractRes = req.execute()
        if (!uploadContractRes.isSuccessful) {
            return null
        }

        val uploadContract = uploadContractRes.body() ?: return null
        val uploadData = uploadContract.toUploadData()

        val requestBody = fileContent.toRequestBody(currentMimeType.toMediaType())
        val multipartBody = MultipartBody.Part.createFormData("file", filename, requestBody)

        val uploadUrl = "https:".plus(uploadContract.args.action)

        val multipartMediaType = "multipart/form-data".toMediaType()
        val req2 = api.uploadMedia(
            uploadUrl = uploadUrl,
            acl = uploadData.acl.toRequestBody(multipartMediaType),
            key = uploadData.key.toRequestBody(multipartMediaType),
            xAmzCredential = uploadData.xAmzCredential.toRequestBody(multipartMediaType),
            xAmzAlgorithm = uploadData.xAmzAlgorithm.toRequestBody(multipartMediaType),
            xAmzDate = uploadData.xAmzDate.toRequestBody(multipartMediaType),
            successActionStatus = uploadData.successActionStatus.toRequestBody(multipartMediaType),
            contentType = uploadData.contentType.toRequestBody(multipartMediaType),
            xAmzStorageClass = uploadData.xAmzStorageClass.toRequestBody(multipartMediaType),
            xAmzMetaExt = uploadData.xAmzMetaExt.toRequestBody(multipartMediaType),
            policy = uploadData.policy.toRequestBody(multipartMediaType),
            xAmzSignature = uploadData.xAmzSignature.toRequestBody(multipartMediaType),
            xAmzSecurityToken = uploadData.xAmzSecurityToken.toRequestBody(multipartMediaType),
            file = multipartBody
        )

        val res2 = req2.execute()
        if (!res2.isSuccessful) {
            return null
        }

        return uploadUrl.plus('/').plus(uploadData.key)
    }
}
