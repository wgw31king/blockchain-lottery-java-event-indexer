package com.example.blockchainlottery.infrastructure.decoder;

import com.example.blockchainlottery.infrastructure.config.LotteryChainProperties.EventDefinition;
import java.util.ArrayList;
import java.util.List;

public final class ErcStandardEvents {

    private ErcStandardEvents() {
    }

    public static List<EventDefinition> definitions(
            boolean erc721Transfer,
            boolean erc1155TransferSingle,
            boolean erc1155TransferBatch
    ) {
        List<EventDefinition> list = new ArrayList<>();
        if (erc721Transfer) {
            list.add(transferErc721());
        }
        if (erc1155TransferSingle) {
            list.add(transferSingleErc1155());
        }
        if (erc1155TransferBatch) {
            list.add(transferBatchErc1155());
        }
        return list;
    }

    /**
     * ERC-721 Transfer(address indexed from, address indexed to, uint256 indexed tokenId).
     */
    private static EventDefinition transferErc721() {
        EventDefinition def = new EventDefinition();
        def.setName("Transfer");
        def.setSignature("Transfer(address,address,uint256)");
        def.setIndexedParams(List.of("from:address", "to:address", "tokenId:uint256"));
        def.setDataParams(List.of());
        return def;
    }

    /**
     * ERC-1155 TransferSingle(address indexed operator, address indexed from, address indexed to, uint256 id, uint256 value).
     */
    private static EventDefinition transferSingleErc1155() {
        EventDefinition def = new EventDefinition();
        def.setName("TransferSingle");
        def.setSignature("TransferSingle(address,address,address,uint256,uint256)");
        def.setIndexedParams(List.of("operator:address", "from:address", "to:address"));
        def.setDataParams(List.of("id:uint256", "value:uint256"));
        return def;
    }

    /**
     * ERC-1155 TransferBatch(address indexed operator, address indexed from, address indexed to, uint256[] ids, uint256[] values).
     */
    private static EventDefinition transferBatchErc1155() {
        EventDefinition def = new EventDefinition();
        def.setName("TransferBatch");
        def.setSignature("TransferBatch(address,address,address,uint256[],uint256[])");
        def.setIndexedParams(List.of("operator:address", "from:address", "to:address"));
        def.setDataParams(List.of("ids:uint256[]", "values:uint256[]"));
        return def;
    }
}
