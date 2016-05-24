#include <stdio.h>
#include <stdlib.h>

#define MAXDATA 300000
#define MAXCODE 10000
#define MAXLENGTH 100
#define INSTR_BAD -1
#define INSTR_LD  1
#define INSTR_ST  2
#define INSTR_LDC 3
#define INSTR_ADD 4
#define INSTR_SUB 5
#define INSTR_CMP 6
#define INSTR_JMP 7
#define INSTR_BR  8
#define INSTR_RET 9

typedef struct label {
    char name[MAXLENGTH];
    size_t line;
    struct label *next;
} label;

typedef struct instruction {
    int type;
    union arg {
        size_t adr;
        int number;
        char label[MAXLENGTH];
    } arg;
} instruction;

size_t find_label (char * s, label * head) {
    while (head != NULL) {
        if (!strcmp(head->name, s)) {
            return head->line;
        }
        else {
            head = head->next;
        }
    }
    return -1;
}

void perform (instruction * code, int j, int * my_stack, int * data, label * head) {
    int stop = 0, error = 0, sum = 0;
    size_t i = 0, st = 0;
    while (!stop && !error && i < j) {
        switch (code[i].type) {

            case INSTR_BAD:
                error = 1;
                break;

            case INSTR_RET:
                stop = 1;
                break;

            case INSTR_LD:
                my_stack[st] = data[code[i].arg.adr];
                st++;
                i++;
                break;

            case INSTR_ST:
                if (st != 0) {
                    data[code[i].arg.adr] = my_stack[st - 1];
                    st--;
                    i++;
                }
                else {
                    error = 1;
                }
                break;

            case INSTR_LDC:
                my_stack[st] = code[i].arg.number;
                st++;
                i++;
                break;

            case INSTR_ADD:
                if (st > 1) {
                    sum = my_stack[st - 1] + my_stack[st - 2];
                    st--;
                    my_stack[st - 1] = sum;
                    i++;
                }
                else {
                    error = 1;
                }
                break;

            case INSTR_SUB:
                if (st > 1) {
                    sum = my_stack[st - 1] - my_stack[st - 2];
                    st--;
                    my_stack[st - 1] = sum;
                    i++;
                }
                else {
                    error = 1;
                }
                break;


            case INSTR_CMP:
                if (st > 1) {
                    if (my_stack[st - 1] > my_stack[st - 2]) {
                        st--;
                        my_stack[st - 1] = 1;
                    }
                    else if(my_stack[st - 1] < my_stack[st - 2]) {
                        st--;
                        my_stack[st - 1] = -1;
                    }
                    else {
                        st--;
                        my_stack[st - 1] = 0;
                    }
                    i++;
                }
                else {
                    error = 1;
                }
                break;

            case INSTR_JMP:
                i = find_label (code[i].arg.label, head);
                if (i == -1) {
                    error = 1;
                }
                break;

            case INSTR_BR:
                if (my_stack[st - 1] != 0) {
                    i = find_label (code[i].arg.label, head);
                    if (i == -1) {
                        error = 1;
                    }
                }
                else {
                    i++;
                }
            break;

            default:
                error = 1;
                break;
        }
    }

	if (error == 1) {
        printf("THERE ARE ERRORS IN THIS CODE");
	}

//    int k = 0;
//	for(k = 0; k < st; ++k) {
//        printf("%d", my_stack[k]);
//	}
//    printf("%d", data[3]);
}

void add_instruction (char * s, int num, instruction * code, int j, char * lbl) {
    if (!strcmp(s, "ld")) {
        code[j].type = INSTR_LD;
        code[j].arg.adr = num;
    }
    if (!strcmp(s, "st")) {
        code[j].type = INSTR_ST;
        code[j].arg.adr = num;
    }
    if (!strcmp(s, "ldc")) {
        code[j].type = INSTR_LDC;
        code[j].arg.number = num;
    }
    if (!strcmp(s, "add")) {
        code[j].type = INSTR_ADD;
    }
    if (!strcmp(s, "sub")) {
        code[j].type = INSTR_SUB;
    }
    if (!strcmp(s, "cmp")) {
        code[j].type = INSTR_CMP;
    }
    if (!strcmp(s, "jmp")) {
        code[j].type = INSTR_JMP;
        strcpy(code[j].arg.label, lbl);
    }
    if (!strcmp(s, "br")) {
        code[j].type = INSTR_BR;
        strcpy(code[j].arg.label, lbl);
    }
    if (!strcmp(s, "ret")) {
        code[j].type = INSTR_RET;
    }
}

void interpreter (label * head, instruction * code, int * my_stack, int * data) {
    int n = 0, num = 0, sign = 1, is_label = 0;
    size_t i = 0, j = 0, for_s = 0;
    char * t = (char *) malloc(sizeof(char) * MAXLENGTH);
    if (t == NULL) {
        printf ("ERROR\n");
    }
    char * s = (char *) malloc(sizeof(char) * MAXLENGTH);
    if (s == NULL) {
        printf ("ERROR\n");
    }
    char * lbl = (char *) malloc(sizeof(char) * MAXLENGTH);
    if (lbl == NULL) {
        printf ("ERROR\n");
    }
    FILE * input;
    input = fopen ("input.txt", "r");
    if(input == NULL) {
        printf ("CAN'T OPEN input.txt\n");
    }
    while (fgets(t, MAXLENGTH, input) != NULL) {
        n = strlen(t);
        for (i = 0; i < n; ++i) {
            if ((int)(t[i]) >= (int)('a') && (int)(t[i]) <= (int)('z')) {
                s[for_s] = t[i];
                for_s++;
            }
            else if (t[i] == ':') {
                label * tmp = (label *) malloc(sizeof(label));
                if (tmp == NULL) {
                    printf("ERROR");
                    return 0;
                }
                s[for_s] = '\0';
                strcpy(tmp->name, s);
                tmp->line = j;
                tmp->next = head->next;
                head->next = tmp;
                for_s = 0;
            }
            else if (t[i] == '-') {
                sign = -1;
            }
            else if ((int)(t[i]) >= (int)('0') && (int)(t[i]) <= (int)('9')) {
                num = num * 10 + (int)(t[i]) - (int)('0');
            }
            s[for_s] = '\0';
            if (!strcmp(s, "jmp") || !strcmp(s, "br")) {
                strcpy(lbl, s);
                for_s = 0;
                is_label = 1;
            }
            if (i == n - 1 && for_s != 0 && is_label == 0) {
                num = num * sign;
                add_instruction(s, num, code, j, lbl);
                j++;
                num = 0;
                for_s = 0;
                sign = 1;
                is_label = 0;
            }
            else if (i == n - 1 && is_label == 1) {
                add_instruction(lbl, num, code, j, s);
                j++;
                for_s = 0;
                is_label = 0;
            }
        }
    }

    perform(code, j, my_stack, data, head);

	if (fclose(input)) {
		printf("Ошибка при закрытии файла.\n");
	}
}

int main()
{
    label * head = (label *) malloc(sizeof(label));
    if(head == NULL) {
        printf("ERROR");
        return 0;
    }

    int * data = (int *) malloc(sizeof(int) * MAXDATA);
	if(data == NULL) {
		printf("ERROR\n");
	}

	int * my_stack = (int *) malloc(sizeof(int) * MAXDATA);
	if(my_stack == NULL) {
		printf("ERROR\n");
	}

	instruction * code = (instruction *) malloc(sizeof(int) * MAXCODE);
	if(code == NULL) {
		printf("ERROR\n");
	}

    interpreter(head, code, my_stack, data);

    return 0;
}
