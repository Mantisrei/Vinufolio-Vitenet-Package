package net.vite.wallet.balance.walletconnect.taskdetail.buildin

import io.reactivex.schedulers.Schedulers
import net.vite.wallet.TokenInfoCenter
import net.vite.wallet.abi.datatypes.Address
import net.vite.wallet.abi.datatypes.TokenId
import net.vite.wallet.balance.walletconnect.taskdetail.DataDecodeResult
import net.vite.wallet.balance.walletconnect.taskdetail.ParseException
import net.vite.wallet.balance.walletconnect.taskdetail.WCConfirmInfo
import net.vite.wallet.balance.walletconnect.taskdetail.WCConfirmItemInfo
import net.vite.wallet.constants.WcDesc
import net.vite.wallet.constants.WcLangItem
import net.vite.wallet.constants.WcNameItem
import net.vite.wallet.network.http.vitex.NormalTokenInfo
import org.walletconnect.Session
import java.math.BigInteger
import java.util.concurrent.CountDownLatch

class DexFundTransferTokenOwner : Decoder {
    private val desc = WcDesc(
        function = WcNameItem(
            name = WcLangItem(
                base = "Transfer Token's Ownership",
                zh = "转移币种权限"
            )
        ),
        inputs = listOf(
            WcNameItem(WcLangItem(base = "Token Name", zh = "代币全称")),
            WcNameItem(WcLangItem(base = "Token Symbol", zh = "代币简称")),
            WcNameItem(WcLangItem(base = "Referral Code", zh = "邀请码"))
        )
    )

    override fun decode(
        rawSendTransaction: Session.MethodCall.SendTransaction,
        abiDataParseResult: List<DataDecodeResult>,
        normalTokenInfo: NormalTokenInfo?
    ): WCConfirmInfo {
        return with(rawSendTransaction) {
            if (block.amount.toBigInteger() != BigInteger.ZERO) {
                throw ParseException.amountError("block.amount in DexFundTransferTokenOwner must zero but now value is ${block.amount}")
            }

            val tokenIdValue = if (abiDataParseResult[0].value is TokenId) {
                abiDataParseResult[0].value.value.toString()
            } else {
                throw ParseException.dataError("DexFundTransferTokenOwner data 0 is not TokenId")
            }

            val address = if (abiDataParseResult[1].value is Address) {
                abiDataParseResult[1].value.value.toString()
            } else {
                throw ParseException.dataError("DexFundTransferTokenOwner data 1 is not Address")
            }
            var result: Pair<WCConfirmInfo?, Throwable?>? = null
            val countDownLatch = CountDownLatch(1)

            TokenInfoCenter.queryViteToken(tokenIdValue)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    result = WCConfirmInfo(
                        title = desc.title(),
                        listItems = listOf(
                            WCConfirmItemInfo.create(desc.inputs[0], it.name ?: ""),
                            WCConfirmItemInfo.create(desc.inputs[1], it.uniqueName()),
                            WCConfirmItemInfo.create(desc.inputs[2], address)
                        )
                    ) to null
                    countDownLatch.countDown()

                }, {
                    result = null to ParseException.dataError(
                        "DexFundTransferTokenOwner cannot find " +
                                "tokenid($tokenIdValue)`s tokeninfo ${it.message}"
                    )
                    countDownLatch.countDown()
                })
            countDownLatch.await()
            result?.second?.let {
                throw it
            }
            result?.first!!
        }
    }
}