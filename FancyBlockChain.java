public class FancyBlockChain {
    public Block[] bchain;
    public int size;
    public int maxTimestamp;

    public FancyBlockChain(int capacity) {
        this.bchain = new Block[capacity];
        this.size = 0;
        this.maxTimestamp = 0;
    }
    public FancyBlockChain(Block[] initialBlocks) {
        this.bchain = new Block[initialBlocks.length];
        this.size = initialBlocks.length;
        for (int i = 0; i < initialBlocks.length; i++) {
            bchain[i] = initialBlocks[i];
            bchain[i].index = i;
            if (bchain[i].timestamp > this.maxTimestamp) {
                this.maxTimestamp = bchain[i].timestamp;
            }
        }

        int beg_index = (length() / 2) - 1;
        for (int i = beg_index; i >= 0; i--) {
            heapify(i);
        }
    }
    public void heapify(int index) {
        int leftChild = 2 * index + 1;
        int rightChild = 2 * index + 2;
        int smallest = index;

        if (leftChild < length()) {
            if (bchain[leftChild].timestamp < bchain[smallest].timestamp) {
                smallest = leftChild;
            }
        }
        if (rightChild < length()) {
            if (bchain[rightChild].timestamp < bchain[smallest].timestamp) {
                smallest = rightChild;
            }
        }

        if (smallest != index) {
            Block temp = bchain[index];
            bchain[index] = bchain[smallest];
            bchain[smallest] = temp;

            bchain[index].index = index;
            bchain[smallest].index = smallest;

            heapify(smallest);
        }
    }

    public int length() {
        return this.size;
    }

    public boolean addBlock(Block newBlock) {
        if (newBlock.timestamp > this.maxTimestamp) {
            this.maxTimestamp = newBlock.timestamp;
        }
        if (length() < bchain.length) {
            bchain[length()] = newBlock;
            bchain[length()].index = length();
            swimUp(length());
            size++;
            return true;
        }

        if (newBlock.timestamp > bchain[0].timestamp) {
            bchain[0] = newBlock;
            bchain[0].index = 0;
            heapify(0);
            return true;
        }

        return false;
    }

    public void swimUp(int index) {
        int parentIndex = (index - 1) / 2;
        while (index > 0 && bchain[index].timestamp < bchain[parentIndex].timestamp) {
            Block temp = bchain[index];
            bchain[index] = bchain[parentIndex];
            bchain[parentIndex] = temp;
            bchain[index].index = index;
            bchain[parentIndex].index = parentIndex;
            index = parentIndex;

            parentIndex = (index - 1) / 2;
        }
    }

    public Block getEarliestBlock() {
        if (length() > 0) {
            return bchain[0];
        }
        return null;
    }


    public Block getBlock(String data) {
        for (int i = 0; i < length(); i++) {
            if (bchain[i].data.equals(data)) {
                return bchain[i];
            }
        }
        return null;
    }

    public Block removeEarliestBlock() {
        if (bchain[0] == null) {
            return null;
        }
        Block earliestBlock = bchain[0];
        bchain[0] = bchain[length() - 1];
        bchain[0].index = 0;
        earliestBlock.removed = true;
        earliestBlock.index = -1;

        this.size--;

        heapify(0);

        return earliestBlock;
    }


    public Block removeBlock(String data) {
        int removalIndex = -1;

        for (int i = 0; i < length(); i++) {
            if (bchain[i] != null && bchain[i].data.equals(data)) {
                removalIndex = i;
                break;
            }
        }

        if (removalIndex == -1) {
            return null;
        }

        Block removedBlock = bchain[removalIndex];
        bchain[removalIndex] = bchain[length() - 1];
        bchain[removalIndex].index = removalIndex;
        removedBlock.removed = true;
        removedBlock.index = -1;

        this.size--;

        if (removalIndex < length()) {
            boolean leftChildExists = 2 * removalIndex + 1 < length();
            boolean rightChildExists = 2 * removalIndex + 2 < length();

            if ((leftChildExists && bchain[removalIndex].timestamp > bchain[2 * removalIndex + 1].timestamp)
                    || (rightChildExists && bchain[removalIndex].timestamp > bchain[2 * removalIndex + 2].timestamp)) {
                heapify(removalIndex);
            }
        }
        if (removalIndex > 0 && removalIndex < length()) {
            if (bchain[removalIndex].timestamp < bchain[(removalIndex - 1) / 2].timestamp) {
                swimUp(removalIndex);
            }
        }

        return removedBlock;
    }

    public void updateEarliestBlock(double nonce) {
        if (length() == 0) {
            return;
        }

        bchain[0].nonce = nonce;
        bchain[0].timestamp = 1 + this.maxTimestamp;
        this.maxTimestamp = bchain[0].timestamp;
        heapify(0);
    }
    public void updateBlock(String data, double nonce) {
        int found = -1;
        int updated_index = -1;

        for (int i = 0; i < length(); i++) {
            if (bchain[i].data.equals(data)) {
                found++;
                bchain[i].nonce = nonce;
                bchain[i].timestamp = 1 + this.maxTimestamp;
                this.maxTimestamp = bchain[i].timestamp;
                updated_index = i;
            }
        }
        if (found != -1) {
            if (updated_index < length() - 1) {
                if (bchain[updated_index].timestamp > bchain[updated_index + 1].timestamp) {
                    heapify(updated_index);
                }
            }
            if (updated_index > 0) {
                if (bchain[updated_index].timestamp < bchain[(updated_index - 1) / 2].timestamp) {
                    swimUp(updated_index);
                }
            }
        }
    }

}
