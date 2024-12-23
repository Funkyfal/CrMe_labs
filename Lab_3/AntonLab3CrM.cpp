#include <iostream>
#include <fstream>
#include <vector>
#include <bitset>
#include <cstdint>
#include <cstring>

using namespace std;

const int IP[64] = {
    58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20, 12, 4,
    62, 54, 46, 38, 30, 22, 14, 6, 64, 56, 48, 40, 32, 24, 16, 8,
    57, 49, 41, 33, 25, 17, 9, 1, 59, 51, 43, 35, 27, 19, 11, 3,
    61, 53, 45, 37, 29, 21, 13, 5,63, 55, 47, 39, 31, 23, 15, 7
};

const int IP_INV[64] = {
    40, 8, 48, 16, 56, 24, 64, 32, 39, 7, 47, 15, 55, 23, 63, 31,
    38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29,
    36, 4, 44, 12, 52, 20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27,
    34, 2, 42, 10, 50, 18, 58, 26, 33, 1, 41, 9, 49, 17, 57, 25
};

const int E[48] = {
    32, 1, 2, 3, 4, 5,
    4, 5, 6, 7, 8, 9,
    8, 9, 10, 11, 12, 13,
    12, 13, 14, 15, 16, 17,
    16, 17, 18, 19, 20, 21,
    20, 21, 22, 23, 24, 25,
    24, 25, 26, 27, 28, 29,
    28, 29, 30, 31, 32, 1
};

const int PC1[56] = {
    57, 49, 41, 33, 25, 17, 9, 1, 58, 50, 42, 34, 26, 18,
    10, 2, 59, 51, 43, 35, 27, 19, 11, 3, 60, 52, 44, 36,
    63, 55, 47, 39, 31, 23, 15, 7, 62, 54, 46, 38, 30, 22,
    14, 6, 61, 53, 45, 37, 29, 21, 13, 5, 28, 20, 12, 4
};

const int P[32] = {
    16, 7, 20, 21, 29, 12, 28, 17,
    1, 15, 23, 26, 5, 18, 31, 10,
    2, 8, 24, 14, 32, 27, 3, 9,
    19, 13, 30, 6, 22, 11, 4, 25
};

const int PC2[48] = {
    14, 17, 11, 24, 1, 5, 3, 28, 15, 6, 21, 10, 23, 19, 12, 4,
    26, 8, 16, 7, 27, 20, 13, 2, 41, 52, 31, 37, 47, 55, 30, 40,
    51, 45, 33, 48, 44, 49, 39, 56, 34, 53, 46, 42, 50, 36, 29, 32
};

const int SHIFT_TABLE[16] = {
    1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1
};

const int S_BOXES[8][4][16] = {

    {{14, 4, 13, 1, 2, 15, 11, 8, 3, 10, 6, 12, 5, 9, 0, 7},
     {0, 15, 7, 4, 14, 2, 13, 1, 10, 6, 12, 11, 9, 5, 3, 8},
     {4, 1, 14, 8, 13, 6, 2, 11, 15, 12, 9, 7, 3, 10, 5, 0},
     {15, 12, 8, 2, 4, 9, 1, 7, 5, 11, 3, 14, 10, 0, 6, 13}},

    {{15, 1, 8, 14, 6, 11, 3, 4, 9, 7, 2, 13, 12, 0, 5, 10},
     {3, 13, 4, 7, 15, 2, 8, 14, 12, 0, 1, 10, 6, 9, 11, 5},
     {0, 14, 7, 11, 10, 4, 13, 1, 5, 8, 12, 6, 9, 3, 2, 15},
     {13, 8, 10, 1, 3, 15, 4, 2, 11, 6, 7, 12, 0, 5, 14, 9}},

    {{10, 0, 9, 14, 6, 3, 15, 5, 1, 13, 12, 7, 11, 4, 2, 8},
     {13, 7, 0, 9, 3, 4, 6, 10, 2, 8, 5, 14, 12, 11, 15, 1},
     {13, 6, 4, 9, 8, 15, 3, 0, 11, 1, 2, 12, 5, 10, 14, 7},
     {1, 10, 13, 0, 6, 9, 8, 7, 4, 15, 14, 3, 11, 5, 2, 12}},

    {{7, 13, 14, 3, 0, 6, 9, 10, 1, 2, 8, 5, 11, 12, 4, 15},
     {13, 8, 11, 5, 6, 15, 0, 3, 4, 7, 2, 12, 1, 10, 14, 9},
     {10, 6, 9, 0, 12, 11, 7, 13, 15, 1, 3, 14, 5, 2, 8, 4},
     {3, 15, 0, 6, 10, 1, 13, 8, 9, 4, 5, 11, 12, 7, 2, 14}},

    {{2, 12, 4, 1, 7, 10, 11, 6, 8, 5, 3, 15, 13, 0, 14, 9},
     {14, 11, 2, 12, 4, 7, 13, 1, 5, 0, 15, 10, 3, 9, 8, 6},
     {4, 2, 1, 11, 10, 13, 7, 8, 15, 9, 12, 5, 6, 3, 0, 14},
     {11, 8, 12, 7, 1, 14, 2, 13, 6, 15, 0, 9, 10, 4, 5, 3}},

    {{12, 1, 10, 15, 9, 2, 6, 8, 0, 13, 3, 4, 14, 7, 5, 11},
     {10, 15, 4, 2, 7, 12, 9, 5, 6, 1, 13, 14, 0, 11, 3, 8},
     {9, 14, 15, 5, 2, 8, 12, 3, 7, 0, 4, 10, 1, 13, 11, 6},
     {4, 3, 2, 12, 9, 5, 15, 10, 11, 14, 1, 7, 6, 0, 8, 13}},

    {{4, 11, 2, 14, 15, 0, 8, 13, 3, 12, 9, 7, 5, 10, 6, 1},
     {13, 0, 11, 7, 4, 9, 1, 10, 14, 3, 5, 12, 2, 15, 8, 6},
     {1, 4, 11, 13, 12, 3, 7, 14, 10, 15, 6, 8, 0, 5, 9, 2},
     {6, 11, 13, 8, 1, 4, 10, 7, 9, 5, 0, 15, 14, 2, 3, 12}},

    {{13, 2, 8, 4, 6, 15, 11, 1, 10, 9, 3, 14, 5, 0, 12, 7},
     {1, 15, 13, 8, 10, 3, 7, 4, 12, 5, 6, 11, 0, 14, 9, 2},
     {7, 11, 4, 1, 9, 12, 14, 2, 0, 6, 10, 13, 15, 3, 5, 8},
     {2, 1, 14, 7, 4, 10, 8, 13, 15, 12, 9, 0, 3, 5, 6, 11}}
};

template <size_t InputSize, size_t OutputSize>
void performPermutation(const bitset<InputSize>& source, bitset<OutputSize>& destination, const int* permTable, int permLength) {
    for (int index = 0; index < permLength; ++index) {
        destination[permLength - index - 1] = source[InputSize - permTable[index]];
    }
}

void keys(const bitset<64>& key, vector<bitset<48>>& subkeys) {
    bitset<56> p_Key;
    performPermutation(key, p_Key, PC1, 56);

    bitset<28> C, D;
    for (int i = 0; i < 28; ++i) {
        C[27 - i] = p_Key[55 - i];
        D[27 - i] = p_Key[27 - i];
    }

    for (int i = 0; i < 16; ++i) {
        for (int shift = 0; shift < SHIFT_TABLE[i]; ++shift) {
            C = (C << 1) | bitset<28>(C[27]);
            D = (D << 1) | bitset<28>(D[27]);
        }

        bitset<56> combined;
        for (int i = 0; i < 28; ++i) {
            combined[55 - i] = C[27 - i];
            combined[27 - i] = D[27 - i];
        }

        bitset<48> subkey;
        performPermutation(combined, subkey, PC2, 48);
        subkeys.push_back(subkey);
    }
}

bitset<32> applyFeistelFunction(const bitset<32>& inputBlock, const bitset<48>& roundKey) {
    bitset<48> expandedBlock;
    performPermutation(inputBlock, expandedBlock, E, 48);

    expandedBlock ^= roundKey;

    bitset<32> resultBlock;
    for (int sboxIndex = 0; sboxIndex < 8; ++sboxIndex) {
        int rowIndex = (expandedBlock[47 - 6 * sboxIndex] << 1) | expandedBlock[47 - 6 * sboxIndex - 5];
        int columnIndex = (expandedBlock[47 - 6 * sboxIndex - 1] << 3) | (expandedBlock[47 - 6 * sboxIndex - 2] << 2) |
            (expandedBlock[47 - 6 * sboxIndex - 3] << 1) | expandedBlock[47 - 6 * sboxIndex - 4];
        int substitutionValue = S_BOXES[sboxIndex][rowIndex][columnIndex];

        for (int bitPos = 0; bitPos < 4; ++bitPos) {
            resultBlock[31 - 4 * sboxIndex - bitPos] = (substitutionValue >> bitPos) & 1;
        }
    }

    bitset<32> finalResult;
    performPermutation(resultBlock, finalResult, P, 32);
    return finalResult;
}

bitset<64> processDESBlock(const bitset<64>& blockData, const vector<bitset<48>>& roundKeys, bool decryptMode) {
    bitset<64> permutedBlock;
    performPermutation(blockData, permutedBlock, IP, 64);

    bitset<32> leftHalf, rightHalf;
    for (int bitIndex = 0; bitIndex < 32; ++bitIndex) {
        leftHalf[31 - bitIndex] = permutedBlock[63 - bitIndex];
        rightHalf[31 - bitIndex] = permutedBlock[31 - bitIndex];
    }

    for (int round = 0; round < 16; ++round) {
        bitset<32> temp = rightHalf;
        int keyIndex = decryptMode ? 15 - round : round;
        rightHalf = leftHalf ^ applyFeistelFunction(rightHalf, roundKeys[keyIndex]);
        leftHalf = temp;
    }

    bitset<64> preOutputBlock;
    for (int bitIndex = 0; bitIndex < 32; ++bitIndex) {
        preOutputBlock[63 - bitIndex] = rightHalf[31 - bitIndex];
        preOutputBlock[31 - bitIndex] = leftHalf[31 - bitIndex];
    }

    bitset<64> finalBlock;
    performPermutation(preOutputBlock, finalBlock, IP_INV, 64);
    return finalBlock;
}

void readDataFromFile(const string& filePath, vector<bitset<64>>& dataBlocks) {
    ifstream inputFile(filePath, ios::binary);
    if (!inputFile) {
        cerr << "Ошибка при открытии файла: " << filePath << endl;
        exit(1);
    }

    while (!inputFile.eof()) {
        uint64_t buffer = 0;
        inputFile.read(reinterpret_cast<char*>(&buffer), sizeof(buffer));
        if (inputFile.gcount() > 0) {
            dataBlocks.push_back(bitset<64>(buffer));
        }
    }
    inputFile.close();
}


void writeDataToFile(const string& filePath, const vector<bitset<64>>& dataBlocks) {
    ofstream outputFile(filePath, ios::binary);
    if (!outputFile) {
        cerr << "Ошибка при записи файла: " << filePath << endl;
        exit(1);
    }

    for (const auto& block : dataBlocks) {
        uint64_t buffer = block.to_ullong();
        outputFile.write(reinterpret_cast<char*>(&buffer), sizeof(buffer));
    }
    outputFile.close();
}

bitset<64> tripleDesProcessBlock(const bitset<64>& block, const vector<vector<bitset<48>>>& keys, bool decrypt) {
    if (decrypt) {
        return processDESBlock(
            processDESBlock(
                processDESBlock(block, keys[2], false), keys[1], true),
            keys[0], false);
    }
    else {
        return processDESBlock(
            processDESBlock(
                processDESBlock(block, keys[0], true), keys[1], false),
            keys[2], true);
    }
}

void incrementCounter(bitset<64>& counter) {
    for (int i = 0; i < 64; ++i) {
        if (counter[i] == 0) {
            counter[i] = 1;
            break;
        }
        else {
            counter[i] = 0;
        }
    }
}


int main() {
    setlocale(0, "");
    vector<bitset<64>> blocks;
    bitset<64> key1, key2, key3;
    bitset<64> sync;

    readDataFromFile("in.bin", blocks);

    ifstream keyFile("key.bin", ios::binary | ios::ate);
    if (!keyFile) {
        cerr << "Ошибка: Не удалось открыть файл key.bin" << endl;
        return 1;
    }

    streamsize keyFileSize = keyFile.tellg();
    if (keyFileSize != 24) {
        cerr << "Ошибка: Файл key.bin должен содержать ровно 24 байта." << endl;
        return 1;
    }

    keyFile.seekg(0, ios::beg);
    vector<bitset<64>> keysVector;
    readDataFromFile("key.bin", keysVector);

    if (keysVector.size() >= 3) {
        key1 = keysVector[0];
        key2 = keysVector[1];
        key3 = keysVector[2];
    }
    else {
        cerr << "Не удалось загрузить ключи для Triple DES" << endl;
        return 1;
    }

    vector<bitset<64>> syncVector;
    readDataFromFile("sync.bin", syncVector);
    if (!syncVector.empty()) {
        sync = syncVector[0];
    }

    vector<vector<bitset<48>>> tripleKeys(3);
    keys(key1, tripleKeys[0]);
    keys(key2, tripleKeys[1]);
    keys(key3, tripleKeys[2]);

    cout << "Введите режим шифрования (ECB, CBC, CFB, OFB, CTR): ";
    string mode;
    cin >> mode;

    if (mode != "ECB" && mode != "CBC" && mode != "CFB" && mode != "OFB" && mode != "CTR") {
        cerr << "Неверный режим шифрования." << endl;
        return 1;
    }

    cout << "Введите операцию (encrypt или decrypt): ";
    string operation;
    cin >> operation;

    bool encryptMode;
    if (operation == "encrypt") {
        encryptMode = true;
    }
    else if (operation == "decrypt") {
        encryptMode = false;
    }
    else {
        cerr << "Неверная операция." << endl;
        return 1;
    }

    if (mode == "ECB") {
        for (auto& block : blocks) {
            block = tripleDesProcessBlock(block, tripleKeys, !encryptMode);
        }
    }
    else if (mode == "CBC") {
        bitset<64> previousBlock = sync;
        for (auto& block : blocks) {
            if (encryptMode) {
                block ^= previousBlock;
                block = tripleDesProcessBlock(block, tripleKeys, false);
                previousBlock = block;
            }
            else {
                bitset<64> decryptedBlock = tripleDesProcessBlock(block, tripleKeys, true);
                decryptedBlock ^= previousBlock;
                previousBlock = block;
                block = decryptedBlock;
            }
        }
    }
    else if (mode == "CFB") {
        bitset<64> feedback = sync;
        for (auto& block : blocks) {
            if (encryptMode) {
                feedback = tripleDesProcessBlock(feedback, tripleKeys, false);
                block ^= feedback;
                feedback = block;
            }
            else {
                bitset<64> temp = block;
                feedback = tripleDesProcessBlock(feedback, tripleKeys, false);
                block ^= feedback;
                feedback = temp;
            }
        }
    }
    else if (mode == "OFB") {
        bitset<64> feedback = sync;
        for (auto& block : blocks) {
            feedback = tripleDesProcessBlock(feedback, tripleKeys, false);
            block ^= feedback;
        }
    }
    else if (mode == "CTR") {
        bitset<64> counter = sync;
        for (auto& block : blocks){
            bitset<64> encryptedCounter = tripleDesProcessBlock(counter, tripleKeys, false);
            block ^= encryptedCounter;
            incrementCounter(counter);
        }
    }


    writeDataToFile("out.bin", blocks);

    cout << "Операция завершена. Результат записан в файл out.bin" << endl;
    return 0;
}