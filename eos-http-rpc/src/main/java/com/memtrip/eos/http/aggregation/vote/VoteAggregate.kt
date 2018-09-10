package com.memtrip.eos.http.aggregation.vote

import com.memtrip.eos.abi.writer.compression.CompressionType
import com.memtrip.eos.core.block.BlockIdDetails
import com.memtrip.eos.core.crypto.EosPrivateKey
import com.memtrip.eos.core.crypto.signature.PrivateKeySigning
import com.memtrip.eos.http.aggregation.AggregateContext
import com.memtrip.eos.http.aggregation.AggregateResponse
import com.memtrip.eos.http.aggregation.AggregateTransaction
import com.memtrip.eos.http.aggregation.vote.actions.VoteArgs
import com.memtrip.eos.http.aggregation.vote.actions.VoteBody

import com.memtrip.eos.http.rpc.ChainApi
import com.memtrip.eos.http.rpc.model.signing.PushTransaction
import com.memtrip.eos.http.rpc.model.transaction.TransactionAuthorization
import com.memtrip.eos.http.rpc.model.transaction.request.Action
import com.memtrip.eos.http.rpc.model.transaction.request.SignedTransaction
import com.memtrip.eos.http.rpc.model.transaction.request.Transaction
import com.memtrip.eos.http.rpc.model.transaction.response.TransactionCommitted
import com.memtrip.eosio.abi.binary.gen.AbiBinaryGen
import io.reactivex.Single
import java.util.Date
import retrofit2.Response
import java.util.Arrays

class VoteAggregate(chainApi: ChainApi) : AggregateTransaction(chainApi) {

    data class Args(
        val voter: String,
        val proxy: String,
        val producers: List<String>
    )

    fun vote(
        args: Args,
        aggregateContext: AggregateContext
    ): Single<AggregateResponse<TransactionCommitted>> {

        return push(
            aggregateContext.expirationDate,
            Arrays.asList(Action(
                "eosio",
                "voteproducer",
                Arrays.asList(TransactionAuthorization(
                    aggregateContext.authorizingAccountName,
                    "active")),
                voteBin(args)
            )),
            aggregateContext.authorizingPrivateKey
        )
    }

    private fun voteBin(args: Args): String {
        return AbiBinaryGen(CompressionType.NONE).squishVoteBody(
            VoteBody(
                "eosio",
                "voteproducer",
                VoteArgs(
                    args.voter,
                    args.proxy,
                    args.producers)
            )
        ).toHex()
    }
}