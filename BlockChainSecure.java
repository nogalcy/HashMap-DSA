import javax.print.attribute.standard.PrinterMessageFromOperator;

public class BlockChainSecure {
    public FancyBlockChain fbc;
    public Block[] btable;

    public int hashLength;

    public BlockChainSecure(int capacity) {
        int primeCapacity = capacity + 1;

        while (!isPrimeSqrt(primeCapacity)) {
            primeCapacity++;
        }
        this.hashLength = primeCapacity;
        fbc = new FancyBlockChain(capacity);
        btable = new Block[this.hashLength];
    }
    public BlockChainSecure(Block[] initialBlocks) {
        int primeCapacity = initialBlocks.length + 1;
        while (!isPrimeSqrt(primeCapacity)) {
            primeCapacity++;
        }
        this.hashLength = primeCapacity;
        fbc = new FancyBlockChain(initialBlocks.length);
        btable = new Block[primeCapacity];

        for (Block block : initialBlocks) {
            addBlockToBoth(block);
        }
    }
    public int length() {
        return fbc.length();
    }

    public boolean addBlock(Block newBlock) {
        if (fbc.length() >= fbc.bchain.length) {
            if (newBlock.timestamp < fbc.getEarliestBlock().timestamp) {
                return false;
            }
            Block removedBlock = fbc.removeEarliestBlock();
            removeBlock(removedBlock.data);
        }

        addBlockToBoth(newBlock);
        return true;
    }

    public Block getEarliestBlock() {
        return fbc.getEarliestBlock();
    }
    public Block getBlock(String data) {
        int hashIndex = findHashIndex(data, false);
        if (hashIndex != -1 && btable[hashIndex] != null && !btable[hashIndex].removed) {
            return btable[hashIndex];
        }
        return null;
    }
    public Block removeEarliestBlock() {
        if (fbc.size == 0) {
            return null;
        }
        Block earliestBlock = fbc.removeEarliestBlock();
        if (earliestBlock != null) {
            removeBlock(earliestBlock.data);
        }
        return earliestBlock;
    }
    public Block removeBlock(String data) {
        Block removedBlock = getBlock(data);
        if (removedBlock == null) {
            return null;
        }
        int removalIndex = removedBlock.index;

        fbc.bchain[removalIndex] = fbc.bchain[length() - 1];
        fbc.bchain[removalIndex].index = removalIndex;
        removedBlock.removed = true;
        removedBlock.index = -1;

        fbc.size--;

        if (removalIndex < length()) {
            boolean leftChildExists = 2 * removalIndex + 1 < length();
            boolean rightChildExists = 2 * removalIndex + 2 < length();

            if ((leftChildExists && fbc.bchain[removalIndex].timestamp > fbc.bchain[2 * removalIndex + 1].timestamp)
                    || (rightChildExists && fbc.bchain[removalIndex].timestamp > fbc.bchain[2 * removalIndex + 2].timestamp)) {
                fbc.heapify(removalIndex);
            }
        }
        if (removalIndex > 0 && removalIndex < length()) {
            if (fbc.bchain[removalIndex].timestamp < fbc.bchain[(removalIndex - 1) / 2].timestamp) {
                fbc.swimUp(removalIndex);
            }
        }

        return removedBlock;

    }
    public void updateEarliestBlock(double nonce) {
        if (fbc.size == 0) {
            return;
        }
        Block earliestBlock = fbc.getEarliestBlock();
        earliestBlock.nonce = nonce;
        earliestBlock.timestamp = 1 + fbc.maxTimestamp;
        fbc.maxTimestamp = earliestBlock.timestamp;

        fbc.heapify(0);
        updateHashNonce(earliestBlock.data, nonce);
    }

    public void updateHashNonce(String data, double nonce) {
        if (getBlock(data) != null) {
            getBlock(data).nonce = nonce;
        }
    }

    public void updateBlock(String data, double nonce) {
        int hashIndex = findHashIndex(data, false);
        Block updatedBlock = null;
        if (hashIndex != -1 && btable[hashIndex] != null && !btable[hashIndex].removed) {
            updatedBlock = btable[hashIndex];
        }

        if (updatedBlock != null) {
            updatedBlock.nonce = nonce;
            updatedBlock.timestamp = 1 + fbc.maxTimestamp;
            fbc.maxTimestamp = updatedBlock.timestamp;

            int updatedIndex = updatedBlock.index;
            if (updatedIndex < length() - 1) {
                if (fbc.bchain[updatedIndex].timestamp > fbc.bchain[updatedIndex + 1].timestamp) {
                    fbc.heapify(updatedIndex);
                }
            }
            if (updatedIndex > 0) {
                if (fbc.bchain[updatedIndex].timestamp < fbc.bchain[(updatedIndex - 1) / 2].timestamp) {
                    fbc.swimUp(updatedIndex);
                }
            }
        }
    }

    public void addBlockToBoth(Block block) {
        int hashIndex = findHashIndex(block.data, true);
        btable[hashIndex] = block;
        block.removed = false;

        fbc.addBlock(block);

    }

    public int findHashIndex(String data, boolean add) {
        int k = 0;
        boolean linearProbe = false;
        int initialHash = doubleHashIndex(data, k);
        int hashIndex = initialHash;

        while (btable[hashIndex] != null) {
            if (btable[hashIndex].data.equals(data) && !add) {
                return hashIndex;
            }
            if (btable[hashIndex].removed && add) {
                return hashIndex;
            }
            k++;
            hashIndex = doubleHashIndex(data, k);
            if (hashIndex == initialHash) {
                linearProbe = true;
                break;
            }
        }

        if (linearProbe) {
            int linearIndex = linearProbingIndex(data, 0, add);
            return linearIndex;
        }
        if (!linearProbe) {
            return hashIndex;
        }

        return -1;
    }

    public int doubleHashIndex(String data, int k) {
        int hash1 = Hasher.hash1(data, this.hashLength);
        int hash2 = Hasher.hash2(data, this.hashLength);
        int hashIndex = (hash1 + k * hash2) % this.hashLength;
        return hashIndex;
    }

    public int linearProbingIndex(String data, int k, boolean add) {
        int hash1 = Hasher.hash1(data, this.hashLength);
        int hashValue = (hash1 + k) % this.hashLength;
        int initialHash = hashValue;
        while (btable[hashValue] != null) {
            if (btable[hashValue].data.equals(data) && !add) {
                return hashValue;
            }
            if (btable[hashValue].removed && add) {
                return hashValue;
            }
            k++;
            hashValue = (hash1 + k) % this.hashLength;
            if (hashValue == initialHash) {
                return -1;
            }
        }
        return hashValue;
    }


    public boolean isPrimeSqrt(int n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }
        int i = 5;
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
            i += 6;
        }
        return true;
    }






}
