package io.horizontalsystems.bitcoinkit.network

import com.nhaarman.mockito_kotlin.whenever
import io.horizontalsystems.bitcoinkit.core.hexStringToByteArray
import io.horizontalsystems.bitcoinkit.io.BitcoinInput
import io.horizontalsystems.bitcoinkit.managers.BloomFilterManager
import io.horizontalsystems.bitcoinkit.models.NetworkAddress
import io.horizontalsystems.bitcoinkit.network.PeerTask.SendTransactionTask
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import java.net.SocketTimeoutException

@RunWith(PowerMockRunner::class)
@PrepareForTest(PeerGroup::class)

class PeerGroupTest {
    private lateinit var peerGroup: PeerGroup

    private var peer1 = mock(Peer::class.java)
    private var peer2 = mock(Peer::class.java)
    private var peerManager = mock(PeerHostManager::class.java)
    private var bloomFilterManager = mock(BloomFilterManager::class.java)
    private var relayTransactionTask = mock(SendTransactionTask::class.java)

    private val peerIp = "8.8.8.8"
    private val peerIp2 = "5.5.5.5"
    private val network = MainNet()

    @Before
    fun setup() {
        whenever(peer1.host).thenReturn(peerIp)
        whenever(peer2.host).thenReturn(peerIp2)
        whenever(peerManager.getPeerIp())
                .thenReturn(peerIp, peerIp2)

        // Peer
        PowerMockito.whenNew(Peer::class.java)
                .withAnyArguments()
                .thenReturn(peer1, peer2)

        // RelayTransactionTask
        PowerMockito.whenNew(SendTransactionTask::class.java)
                .withAnyArguments()
                .thenReturn(relayTransactionTask)

        peerGroup = PeerGroup(peerManager, bloomFilterManager, network, peerSize = 2)
    }

    @Test
    fun run() { // creates peer connection with given IP address
        peerGroup.start()

        Thread.sleep(500L)
        verify(peer1).start()

        // close thread:
        peerGroup.close()
        peerGroup.join()
    }


    @Test
    fun disconnected_withError() { // removes peer from connection list
        peerGroup.onDisconnect(peer1, SocketTimeoutException("Some Error"))

        verify(peerManager).markFailed(peerIp)
    }

//    @Test
//    fun sendPendingTransactions() { // send transaction
//        whenever(peer1.ready).thenReturn(true)
//        peerGroup.onConnect(peer1)
//
//        val transaction = Transaction()
//        peerGroup.sendPendingTransactions(transaction)
//
//        Thread.sleep(100) // wait thread executor
//        verify(peer1).addTask(relayTransactionTask)
//    }

    @Test
    fun onReceiveAddresses() {
        val ip4 = "0A000001"
        val raw = arrayOf("E215104D", "0100000000000000", "00000000000000000000FFFF$ip4", "208D").joinToString("")
        val input = BitcoinInput(raw.hexStringToByteArray())

        val netAddress = NetworkAddress(input, false)

        peerGroup.onReceiveAddress(arrayOf(netAddress))
        verify(peerManager).addPeers(arrayOf("10.0.0.1"))
    }
}
